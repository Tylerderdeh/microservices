package org.example.kafka_redis.controller;

import lombok.RequiredArgsConstructor;
import org.example.kafka_redis.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<String> send(@RequestBody MessageRequest request) {
        String id = messageService.send(request.getPayload(), request.getType());
        return ResponseEntity.ok(id);
    }
}
