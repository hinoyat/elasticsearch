package com.c202.user.user.elastic.controller;

import com.c202.user.user.elastic.document.UserDocument;
import com.c202.user.user.elastic.model.request.UserSearchRequestDto;
import com.c202.user.user.elastic.model.response.UserSearchResponseDto;
import com.c202.user.user.elastic.service.ElasticSearchResetService;
import com.c202.user.user.elastic.service.UserSearchService;
import com.c202.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserSearchController {

    private final UserSearchService userSearchService;
    private final ElasticSearchResetService elasticSearchResetService;
    private final ElasticsearchOperations elasticsearchOperations;

    @GetMapping("/users/search")
    public ResponseEntity<ResponseDto<List<UserSearchResponseDto>>> searchUsers(
            @ModelAttribute UserSearchRequestDto requestDto) {
        log.info("User search request: {}", requestDto);
        List<UserSearchResponseDto> results = userSearchService.searchUsers(requestDto);
        return ResponseEntity.ok(ResponseDto.success(200, "사용자 검색 완료", results));
    }

    @PostMapping("/users/elasticsearch/reset")
    public ResponseEntity<ResponseDto<String>> resetElasticsearch() {
        try {
            elasticSearchResetService.resetAndReindexUsers();
            return ResponseEntity.ok(ResponseDto.success(200, "인덱스 초기화 및 재생성 완료", "success"));
        } catch (Exception e) {
            log.error("인덱스 초기화 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.error(500, "인덱스 초기화 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/users/elasticsearch/health")
    public ResponseEntity<String> checkElasticsearchHealth() {
        try {
            // 간단한 count 쿼리로 ElasticSearch 연결 확인
            long count = elasticsearchOperations.count(NativeQuery.builder().build(), UserDocument.class);
            return ResponseEntity.ok("ElasticSearch is up. Document count: " + count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Error connecting to ElasticSearch: " + e.getMessage());
        }
    }

}