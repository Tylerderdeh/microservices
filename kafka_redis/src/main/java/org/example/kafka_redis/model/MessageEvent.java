package org.example.kafka_redis.model;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class MessageEvent {

    private String id;
    private String payload;
    private String type;
    private Instant timestamp;

    public MessageEvent() {}

    public MessageEvent(String payload, String type) {
        this.id = UUID.randomUUID().toString();
        this.payload = payload;
        this.type = type;
        this.timestamp = Instant.now();
    }

}
