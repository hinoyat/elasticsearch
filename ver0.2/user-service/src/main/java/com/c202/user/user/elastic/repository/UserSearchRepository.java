package com.c202.user.user.elastic.repository;

import com.c202.user.user.elastic.document.UserDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserSearchRepository extends ElasticsearchRepository<UserDocument, Integer> {
}
