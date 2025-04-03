package com.c202.user.user.elastic.service;

import com.c202.user.user.elastic.document.UserDocument;
import com.c202.user.user.entity.User;
import com.c202.user.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticSearchResetService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final UserIndexService userIndexService;
    private final UserRepository userRepository;

    // 인덱스 초기화 및 재생성을 위한 메서드
    @Transactional
    public void resetAndReindexUsers() {
        try {
            // 인덱스 삭제
            elasticsearchOperations.indexOps(UserDocument.class).delete();

            // 인덱스 생성
            elasticsearchOperations.indexOps(UserDocument.class).create();

            // 모든 사용자 재인덱싱
            List<User> users = userRepository.findByIsDeleted("N");
            log.info("인덱싱할 사용자 수: {}", users.size());

            for (User user : users) {
                log.info("인덱싱 사용자: {} ({})", user.getUsername(), user.getUserSeq());
                userIndexService.indexUser(user);
            }

            // 인덱스 확인
            long count = elasticsearchOperations.count(NativeQuery.builder().build(), UserDocument.class);
            log.info("인덱스된 총 사용자 수: {}", count);
        } catch (Exception e) {
            log.error("인덱스 초기화 중 오류 발생: ", e);
            throw new RuntimeException("인덱스 초기화 실패: " + e.getMessage(), e);
        }
    }
}