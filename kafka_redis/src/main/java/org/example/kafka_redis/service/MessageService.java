package org.example.kafka_redis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.kafka_redis.model.MessageEvent;
import org.example.kafka_redis.outbox.OutboxEvent;
import org.example.kafka_redis.outbox.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.main}")
    private String mainTopic;

    @Transactional
    public String send(String payload, String type) {
        MessageEvent event = new MessageEvent(payload, type);
        try {
            String json = objectMapper.writeValueAsString(event);
            outboxRepository.save(new OutboxEvent(mainTopic, json));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize MessageEvent", e);
        }
        return event.getId();
    }
}
