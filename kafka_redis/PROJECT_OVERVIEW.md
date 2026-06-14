# Project Overview — kafka-redis-template

Учебный проект, демонстрирующий production-ready паттерны интеграции Kafka + Redis + PostgreSQL в Spring Boot.

---

## Архитектура

```
┌─────────────────────────────────────────────────────────┐
│                   Spring Boot App                       │
│                                                         │
│  POST /api/messages                                     │
│       │                                                 │
│       ▼                                                 │
│  MessageService ──(tx)──► PostgreSQL [outbox_events]    │
│                                                         │
│  OutboxScheduler (every 5s)                             │
│       │ reads published=false                           │
│       │ executeInTransaction                            │
│       ▼                                                 │
│  KafkaTemplate ─────────────────────────────────────►  │
│                                                         │
│  MessageConsumer ◄──── Kafka [main-topic]               │
│       │ check Redis idempotency                         │
│       │ process                                         │
│       │ (on error after 2 retry) ──► Kafka [main-topic.DLT]
│                                                         │
│  DlqConsumer ◄──── Kafka [main-topic.DLT]              │
│       │ log / alert                                     │
└─────────────────────────────────────────────────────────┘
         │                    │              │
    PostgreSQL             Kafka          Redis
  (outbox_events)    (main-topic,     (idempotency
                      main-topic.DLT)  keys with TTL)
```

---

## Стек

| Технология | Версия | Роль |
|---|---|---|
| Spring Boot | 4.0.6 | Фреймворк |
| Spring Kafka | 4.x | Kafka integration |
| Spring Data Redis | 4.x | Redis client (Lettuce) |
| Spring Data JPA | 4.x | PostgreSQL ORM |
| PostgreSQL | 16 | Хранение outbox событий |
| Apache Kafka | 7.6 (Confluent) | Message broker |
| Redis | 7.2 | Idempotency store |
| Lombok | — | Boilerplate reduction |

---

## Структура пакетов

```
org.example.kafka_redis
├── KafkaRedisApplication.java          # @SpringBootApplication @EnableScheduling
├── config/
│   ├── KafkaProducerConfig.java        # ProducerFactory + KafkaTemplate (transactional)
│   ├── KafkaConsumerConfig.java        # ConsumerFactory + factory + DLQ error handler
│   ├── KafkaTopicConfig.java           # NewTopic beans (main-topic + DLT)
│   └── RedisConfig.java                # RedisTemplate<String, String>
├── model/
│   └── MessageEvent.java               # Kafka message payload
├── outbox/
│   ├── OutboxEvent.java                # @Entity → outbox_events table
│   ├── OutboxEventRepository.java      # JpaRepository, findByPublishedFalse()
│   └── OutboxScheduler.java            # @Scheduled every 5s → publishes to Kafka
├── service/
│   ├── MessageService.java             # saves to outbox (in DB transaction)
│   └── IdempotencyService.java         # Redis SETNX for deduplication
├── consumer/
│   ├── MessageConsumer.java            # @KafkaListener on main-topic
│   └── DlqConsumer.java                # @KafkaListener on main-topic.DLT
└── controller/
    ├── MessageController.java          # POST /api/messages
    └── MessageRequest.java             # DTO
```

---

## Паттерны и зачем они здесь

### 1. Outbox Pattern
**Проблема:** нельзя атомарно сохранить в БД и отправить в Kafka в одной транзакции — это два разных ресурса.

**Реализация:**
- `MessageService.send()` — в JPA-транзакции сохраняет `OutboxEvent(published=false)` в PostgreSQL
- `OutboxScheduler` — каждые 5 сек читает `findByPublishedFalse()`, отправляет в Kafka через `kafkaTemplate.executeInTransaction()`, помечает `published=true`

**Что покрывает:** падение приложения после сохранения в БД — событие не потеряется, scheduler отправит при следующем старте.

---

### 2. Exactly-Once Producer (Idempotent + Transactional)
**Настройка:**
```properties
spring.kafka.producer.acks=all
spring.kafka.producer.enable-idempotence=true
spring.kafka.producer.properties.transactional.id=kafka-redis-tx
```
**Что даёт:**
- `enable.idempotence` + `acks=all` → брокер дедуплицирует повторные отправки по PID+sequence
- `transactional.id` → атомарная отправка нескольких сообщений (или ни одного)
- `kafkaTemplate.executeInTransaction()` в scheduler → Kafka транзакция вокруг каждой отправки

---

### 3. Read Committed Consumer
```properties
spring.kafka.consumer.properties.isolation.level=read_committed
```
Консьюмер не читает сообщения из незакоммиченных или абортированных транзакций. Обязательно при использовании transactional producer.

---

### 4. Idempotent Consumer (Redis)
**Проблема:** Kafka гарантирует at-least-once — консьюмер может получить одно сообщение дважды (при rebalance, сетевом сбое).

**Реализация (`IdempotencyService`):**
```java
Boolean wasSet = redisTemplate.opsForValue()
    .setIfAbsent("processed:" + messageId, "1", Duration.ofSeconds(ttlSeconds));
return Boolean.TRUE.equals(wasSet); // false = дубль
```
`SET NX EX` — атомарная операция. TTL = 86400 сек (1 день). Если `messageId` уже в Redis → пропускаем.

---

### 5. Dead Letter Topic (DLT)
**Настройка в `KafkaConsumerConfig`:**
```java
DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
    (record, ex) -> new TopicPartition(dlqTopic, 0));

DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
```
- 2 retry с паузой 1 сек
- После исчерпания retry → сообщение в `main-topic.DLT`
- `DlqConsumer` логирует неудачные сообщения для ручного разбора / алертинга

---

## Топики

| Топик | Партиции | Реплики | Назначение |
|---|---|---|---|
| `main-topic` | 3 | 1 | Основные события |
| `main-topic.DLT` | 1 | 1 | Необработанные сообщения |

Топики создаются автоматически через `NewTopic` beans в `KafkaTopicConfig` при старте приложения.

---

## Схема БД

```sql
CREATE TABLE outbox_events (
    id          VARCHAR PRIMARY KEY,       -- UUID
    topic       VARCHAR NOT NULL,          -- целевой Kafka топик
    payload     TEXT NOT NULL,             -- JSON сериализованный MessageEvent
    created_at  TIMESTAMP NOT NULL,
    published   BOOLEAN NOT NULL DEFAULT false
);
```

Создаётся автоматически через `spring.jpa.hibernate.ddl-auto=update`.

---

## Запуск

```bash
# 1. Поднять инфраструктуру
docker-compose up -d

# 2. Запустить приложение
./gradlew bootRun

# 3. Отправить тестовое сообщение
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -d '{"payload": "hello", "type": "TEST"}'

# Ответ: UUID сообщения
# В логах появится: "Processing message id=... type=TEST"
```

---

## Поток данных (детально)

```
1. POST /api/messages {"payload":"hello","type":"TEST"}
   └─► MessageController.send()
       └─► MessageService.send()
           └─► new MessageEvent(id=UUID, payload, type, timestamp)
               └─► serialize to JSON
                   └─► save OutboxEvent(topic=main-topic, payload=JSON, published=false)
                       └─► [PostgreSQL tx COMMIT]

2. OutboxScheduler (5 сек спустя)
   └─► findByPublishedFalse() → [OutboxEvent]
       └─► deserialize JSON → MessageEvent
           └─► kafkaTemplate.executeInTransaction(
                   ops.send("main-topic", messageId, messageEvent)
               )
               └─► outboxEvent.setPublished(true) → save

3. MessageConsumer.consume(MessageEvent)
   └─► idempotencyService.tryMarkAsProcessed(event.getId())
       └─► Redis SETNX "processed:{UUID}" "1" EX 86400
           ├─► true (новое) → log.info("Processing...") → бизнес-логика
           └─► false (дубль) → log.warn("Duplicate, skipping")

4. При ошибке в consume():
   └─► DefaultErrorHandler: retry 2 раза (1 сек пауза)
       └─► после исчерпания → DeadLetterPublishingRecoverer
           └─► отправить в "main-topic.DLT"
               └─► DlqConsumer.handleDlq() → log.error(...)
```

---

## Ключевые конфигурационные параметры

```properties
# Producer — максимальная надёжность
spring.kafka.producer.acks=all
spring.kafka.producer.enable-idempotence=true
spring.kafka.producer.properties.transactional.id=kafka-redis-tx

# Consumer — только закоммиченные транзакции
spring.kafka.consumer.properties.isolation.level=read_committed
spring.kafka.consumer.auto-offset-reset=earliest

# Idempotency TTL
idempotency.ttl-seconds=86400

# Topics
kafka.topics.main=main-topic
kafka.topics.dlq=main-topic.DLT
```

---

## Что можно улучшить (для production)

1. **Pessimistic locking в OutboxScheduler** — при нескольких инстансах приложения несколько schedulers попытаются опубликовать одно событие. Решение: `SELECT ... FOR UPDATE SKIP LOCKED` в репозитории
2. **Replication factor > 1** — в docker-compose один брокер, для production нужно минимум 3
3. **Schema Registry** — вместо JSON использовать Avro/Protobuf с Confluent Schema Registry
4. **Monitoring** — Kafka Exporter + Redis Exporter → Prometheus → Grafana
5. **Outbox cleanup job** — периодически удалять старые `published=true` записи чтобы таблица не росла
