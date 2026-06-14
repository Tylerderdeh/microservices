package org.example.kafka_redis.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kafka_redis.model.MessageEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, MessageEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxRepository.findByPublishedFalse();
        for (OutboxEvent event : pending) {
            try {
                MessageEvent msg = objectMapper.readValue(event.getPayload(), MessageEvent.class);
                kafkaTemplate.executeInTransaction(ops -> ops.send(event.getTopic(), msg.getId(), msg));
                event.setPublished(true);
                outboxRepository.save(event);
                log.info("Published outbox event {}", event.getId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
            }
        }
    }
}
