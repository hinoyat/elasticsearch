package com.c202.diary.elastic.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.c202.diary.elastic.document.DiaryDocument;
import com.c202.diary.elastic.model.request.DiarySearchRequestDto;
import com.c202.diary.elastic.model.response.DiarySearchListResponseDto;
import com.c202.diary.elastic.repository.DiarySearchRepository;
import com.c202.diary.emotion.entity.Emotion;
import com.c202.diary.emotion.repository.EmotionRepository;
import com.c202.diary.tag.model.response.TagResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiarySearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final DiarySearchRepository diarySearchRepository;
    private final EmotionRepository emotionRepository;

    public Page<DiarySearchListResponseDto> searchDiaries(DiarySearchRequestDto requestDto, Integer userSeq) {
        // 최상위 조건: 삭제되지 않은 문서, 현재 사용자의 문서 또는 공개 문서
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 삭제되지 않은 일기만 검색
        boolQueryBuilder.must(Query.of(q -> q.term(t -> t.field("isDeleted").value("N"))));

        // 현재 사용자 문서 또는 공개 문서 조건
        if (requestDto.isCurrentUserOnly()) {
            boolQueryBuilder.must(Query.of(q -> q.term(t -> t.field("userSeq").value(userSeq))));
        } else {
            boolQueryBuilder.must(Query.of(q -> q.term(t -> t.field("isPublic").value("Y"))));
        }

        // 키워드가 있으면 제목, 내용, 태그에 대한 검색 조건 추가
        if (requestDto.getKeyword() != null && !requestDto.getKeyword().isEmpty()) {
            BoolQuery.Builder keywordBool = new BoolQuery.Builder();
            String keyword = requestDto.getKeyword();

            if (requestDto.isSearchTitle()) {
                addRobustSearchQueries(keywordBool, "title", keyword);
            }
            if (requestDto.isSearchContent()) {
                addRobustSearchQueries(keywordBool, "content", keyword);
            }
            if (requestDto.isSearchTag()) {
                addRobustSearchQueries(keywordBool, "tags", keyword);
            }

            // 최소한 하나 이상의 절은 매칭되어야 함.
            keywordBool.minimumShouldMatch("1");
            boolQueryBuilder.must(Query.of(q -> q.bool(keywordBool.build())));
        }

        // 페이지 설정 (페이지번호 0부터 시작)
        int page = requestDto.getPage() != null ? requestDto.getPage() - 1 : 0;
        int size = requestDto.getSize() != null ? requestDto.getSize() : 20;
        if(page < 0) {
            page = 0;
        }
        PageRequest pageable = PageRequest.of(page, size);

        // 쿼리 생성 및 실행
        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQueryBuilder.build()))
                .withPageable(pageable)
                .build();
        SearchHits<DiaryDocument> searchHits = elasticsearchOperations.search(searchQuery, DiaryDocument.class);

        // 결과 변환: DiaryDocument → DiarySearchListResponseDto
        List<DiarySearchListResponseDto> results = new ArrayList<>();
        for (SearchHit<DiaryDocument> hit : searchHits) {
            DiaryDocument doc = hit.getContent();
            String emotionName = "";
            if (doc.getEmotionSeq() != null) {
                emotionName = emotionRepository.findById(doc.getEmotionSeq())
                        .map(Emotion::getName)
                        .orElse("");
            }
            List<TagResponseDto> tagDtos = doc.getTags().stream()
                    .map(tag -> TagResponseDto.builder().name(tag).build())
                    .collect(Collectors.toList());
            results.add(DiarySearchListResponseDto.fromDocument(doc, emotionName, tagDtos));
        }

        return new PageImpl<>(results, pageable, searchHits.getTotalHits());
    }

    /**
     * 입력 키워드에 대해 부분 검색을 지원하는 강력한 쿼리를 구성하는 메서드.
     * 다양한 한글 검색 패턴에 모두 대응합니다.
     */
    private void addRobustSearchQueries(BoolQuery.Builder queryBuilder, String fieldName, String keyword) {
        String lowerKeyword = keyword.toLowerCase();

        // 1. 기본 match 쿼리 (단일 토큰 검색)
        queryBuilder.should(Query.of(q -> q
                .match(m -> m
                        .field(fieldName)
                        .query(keyword)
                )
        ));

        // 2. 정확한 구문 검색을 위한 match_phrase
        queryBuilder.should(Query.of(q -> q
                .matchPhrase(mp -> mp
                        .field(fieldName)
                        .query(keyword)
                )
        ));

        // 3. 부분 문자열 검색을 위한 wildcard 쿼리
        queryBuilder.should(Query.of(q -> q
                .wildcard(w -> w
                        .field(fieldName)
                        .wildcard("*" + lowerKeyword + "*")
                )
        ));

        // 4. 각 글자별 AND 검색 (모든 글자가 포함된 경우)
        if (keyword.length() > 1) {
            BoolQuery.Builder charQueryBuilder = new BoolQuery.Builder();

            for (int i = 0; i < keyword.length(); i++) {
                String singleChar = String.valueOf(keyword.charAt(i));
                charQueryBuilder.should(Query.of(q -> q
                        .match(m -> m
                                .field(fieldName)
                                .query(singleChar)
                        )
                ));
            }

            // 모든 글자가 포함되어야 함
            charQueryBuilder.minimumShouldMatch("" + keyword.length());

            queryBuilder.should(Query.of(q -> q.bool(charQueryBuilder.build())));
        }

        // 5. 연속된 부분 문자열 검색 (2글자씩)
        if (keyword.length() > 1) {
            for (int i = 0; i < keyword.length() - 1; i++) {
                String subString = keyword.substring(i, i + 2);
                queryBuilder.should(Query.of(q -> q
                        .match(m -> m
                                .field(fieldName)
                                .query(subString)
                        )
                ));
            }
        }

        // 6. match_phrase_prefix 쿼리 (접두사 매칭)
        queryBuilder.should(Query.of(q -> q
                .matchPhrasePrefix(mpp -> mpp
                        .field(fieldName)
                        .query(keyword)
                )
        ));

        // 7. fuzzy 쿼리 (오타에 강함)
        queryBuilder.should(Query.of(q -> q
                .fuzzy(f -> f
                        .field(fieldName)
                        .value(keyword)
                        .fuzziness("AUTO")
                )
        ));
    }
}