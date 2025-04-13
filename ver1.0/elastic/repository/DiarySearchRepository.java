package com.c202.diary.elastic.repository;

import com.c202.diary.elastic.document.DiaryDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@EnableElasticsearchRepositories
public interface DiarySearchRepository extends ElasticsearchRepository<DiaryDocument, Integer> {
}
