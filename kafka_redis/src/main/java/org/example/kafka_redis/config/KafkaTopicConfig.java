package org.example.kafka_redis.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic mainTopic(@Value("${kafka.topics.main}") String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic dlqTopic(@Value("${kafka.topics.dlq}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }
}
