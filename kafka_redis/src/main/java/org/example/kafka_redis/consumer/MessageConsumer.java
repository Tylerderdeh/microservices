package org.example.kafka_redis.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kafka_redis.model.MessageEvent;
import org.example.kafka_redis.service.IdempotencyService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageConsumer {

    private final IdempotencyService idempotencyService;

    @KafkaListener(topics = "${kafka.topics.main}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(MessageEvent event) {
        if (!idempotencyService.tryMarkAsProcessed(event.getId())) {
            log.warn("Duplicate message {}, skipping", event.getId());
            return;
        }
        log.info("Processing message id={} type={} payload={}", event.getId(), event.getType(), event.getPayload());
        // business logic here
    }
}
