package com.c202.user.user.config;


import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitProducerConfig {

    @Bean
    public Exchange userExchange() {
        return ExchangeBuilder.topicExchange("user.event.exchange").durable(true).build();
    }
}