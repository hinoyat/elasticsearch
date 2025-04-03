package com.c202.user.user.elastic.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.c202.user.user.elastic.document.UserDocument;
import com.c202.user.user.elastic.model.request.UserSearchRequestDto;
import com.c202.user.user.elastic.model.response.UserSearchResponseDto;
import com.c202.user.user.elastic.repository.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final UserSearchRepository userSearchRepository;

    public List<UserSearchResponseDto> searchUsers(UserSearchRequestDto requestDto) {
        log.info("검색 요청: {}", requestDto);

        // 간단한 검색 쿼리 생성
        Query query;

        // 키워드가 있는 경우 검색 조건 추가
        if (requestDto.getKeyword() != null && !requestDto.getKeyword().isEmpty()) {
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

            // 삭제되지 않은 사용자만 검색
            boolQueryBuilder.must(Query.of(q -> q
                    .term(t -> t
                            .field("isDeleted")
                            .value("N")
                    )
            ));

            // 검색 조건 (username, nickname)
            BoolQuery.Builder keywordQueryBuilder = new BoolQuery.Builder();

            if (requestDto.isSearchUsername()) {
                keywordQueryBuilder.should(Query.of(q -> q
                        .match(m -> m
                                .field("username")
                                .query(requestDto.getKeyword())
                        )
                ));
            }

            if (requestDto.isSearchNickname()) {
                keywordQueryBuilder.should(Query.of(q -> q
                        .match(m -> m
                                .field("nickname")
                                .query(requestDto.getKeyword())
                        )
                ));
            }

            boolQueryBuilder.must(Query.of(q -> q.bool(keywordQueryBuilder.build())));
            query = Query.of(q -> q.bool(boolQueryBuilder.build()));
        } else {
            // 키워드가 없는 경우 모든 활성 사용자 검색
            query = Query.of(q -> q
                    .term(t -> t
                            .field("isDeleted")
                            .value("N")
                    )
            );
        }

        // 쿼리 실행
        NativeQuery searchQuery = NativeQuery.builder().withQuery(query).build();
        log.info("검색 쿼리: {}", searchQuery);

        SearchHits<UserDocument> searchHits = elasticsearchOperations.search(
                searchQuery, UserDocument.class);

        log.info("검색 결과 수: {}", searchHits.getTotalHits());

        // 결과 변환
        List<UserSearchResponseDto> result = new ArrayList<>();
        for (SearchHit<UserDocument> hit : searchHits) {
            UserDocument doc = hit.getContent();
            log.info("검색 결과: {} ({})", doc.getUsername(), doc.getUserSeq());
            UserSearchResponseDto userDto = UserSearchResponseDto.fromDocument(doc);
            result.add(userDto);
        }

        return result;
    }
}