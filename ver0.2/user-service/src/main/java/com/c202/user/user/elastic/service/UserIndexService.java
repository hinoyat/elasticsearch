package com.c202.user.user.elastic.service;

import com.c202.user.user.elastic.document.UserDocument;
import com.c202.user.user.elastic.repository.UserSearchRepository;
import com.c202.user.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserIndexService {

    private final UserSearchRepository userSearchRepository;

    public void indexUser(User user) {
        try {
            UserDocument document = convertToDocument(user);
            log.info("사용자 문서 변환: {} -> {}", user.getUsername(), document);
            userSearchRepository.save(document);
            log.info("사용자 인덱싱 완료: {}", user.getUsername());
        } catch (Exception e) {
            log.error("사용자 인덱싱 실패: {} - {}", user.getUsername(), e.getMessage(), e);
        }
    }

    public void deleteUserIndex(Integer userSeq) {
        userSearchRepository.deleteById(userSeq);
    }

    private UserDocument convertToDocument(User user) {
        return UserDocument.builder()
                .userSeq(user.getUserSeq())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .introduction(user.getIntroduction())
                .iconSeq(user.getIconSeq())
                .isDeleted(user.getIsDeleted())
                .build();
    }
}