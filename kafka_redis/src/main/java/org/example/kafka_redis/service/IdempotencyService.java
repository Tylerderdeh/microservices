package org.example.kafka_redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${idempotency.ttl-seconds}")
    private long ttlSeconds;

    /**
     * Returns true if the message is new and was successfully marked as processed.
     * Returns false if it's a duplicate (key already existed in Redis).
     */
    public boolean tryMarkAsProcessed(String messageId) {
        Boolean wasSet = redisTemplate.opsForValue()
                .setIfAbsent("processed:" + messageId, "1", Duration.ofSeconds(ttlSeconds));
        return Boolean.TRUE.equals(wasSet);
    }
}
