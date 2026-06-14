package org.example.kafka_redis.controller;

import lombok.Data;

@Data
public class MessageRequest {
    private String payload;
    private String type;
}
