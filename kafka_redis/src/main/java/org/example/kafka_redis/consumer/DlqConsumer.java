package org.example.kafka_redis.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.kafka_redis.model.MessageEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DlqConsumer {

    @KafkaListener(topics = "${kafka.topics.dlq}", groupId = "kafka-redis-group-dlq")
    public void handleDlq(ConsumerRecord<String, MessageEvent> record) {
        log.error("DLQ message received: key={} topic={} value={}", record.key(), record.topic(), record.value());
        // alert, save to DB for manual review, etc.
    }
}
