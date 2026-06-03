package org.example.kafka_redis.outbox;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
public class OutboxEvent {

    @Id
    private String id;
    private String topic;
    private String payload;
    private Instant createdAt;
    private boolean published;

    public OutboxEvent() {}

    public OutboxEvent(String topic, String payload) {
        this.id = UUID.randomUUID().toString();
        this.topic = topic;
        this.payload = payload;
        this.createdAt = Instant.now();
        this.published = false;
    }
}