package com.c202.user.user.elastic.service;

import com.c202.user.user.entity.User;
import com.c202.user.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ElasticSearchInitService {

    private final UserRepository userRepository;
    private final UserIndexService userIndexService;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void initializeElasticSearch() {
        List<User> users = userRepository.findByIsDeleted("N");

        for (User user : users) {
            userIndexService.indexUser(user);
        }
    }
}