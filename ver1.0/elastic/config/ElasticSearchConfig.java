package com.c202.diary.elastic.config;

import com.c202.diary.elastic.repository.DiarySearchRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackageClasses = DiarySearchRepository.class)
public class ElasticSearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticURL;

    @Override
    public ClientConfiguration clientConfiguration() {
        String host = elasticURL.replaceAll("https?://", "");
        return ClientConfiguration.builder()
                .connectedTo(host)
                .build();
    }
}
