# Kafka & Redis — Теория

---

## KAFKA

### 1. Архитектура

```
Producer → [Broker Cluster] → Consumer Group
                ↑
           ZooKeeper / KRaft (координация)
```

| Компонент | Описание |
|---|---|
| **Broker** | Сервер Kafka, хранит топики |
| **Topic** | Именованный канал сообщений, делится на партиции |
| **Partition** | Упорядоченный, неизменяемый лог. Единица параллелизма |
| **Offset** | Порядковый номер сообщения внутри партиции |
| **Consumer Group** | Группа консьюмеров, каждая партиция читается только одним |
| **Replica** | Копия партиции на другом брокере. Leader принимает запросы |
| **ISR** | In-Sync Replicas — реплики, синхронизированные с лидером |

**Правило:** количество консьюмеров в группе > количества партиций → лишние консьюмеры простаивают.

---

### 2. Producer

#### acks — гарантии доставки от брокера

| acks | Поведение | Риск |
|---|---|---|
| `0` | Не ждёт подтверждения | Потеря данных при любом сбое |
| `1` | Ждёт подтверждения от лидера | Потеря если лидер упал до репликации |
| `all` (`-1`) | Ждёт подтверждения от всех ISR | Максимальная надёжность |

#### Idempotent Producer
```properties
enable.idempotence=true   # автоматически включает acks=all, retries=MAX
```
Kafka присваивает каждому продюсеру **PID** и порядковый номер. Дубли при retry отбрасываются брокером.

#### Транзакции (Exactly-Once)
```properties
transactional.id=my-tx-id
```
```java
producer.initTransactions();
producer.beginTransaction();
producer.send(record1);
producer.send(record2);
producer.commitTransaction(); // либо abortTransaction()
```
Атомарная запись в несколько партиций/топиков. Консьюмер с `isolation.level=read_committed` видит только закоммиченные сообщения.

#### Параметры производительности

| Параметр | По умолчанию | Роль |
|---|---|---|
| `linger.ms` | 0 | Ждать X мс перед отправкой батча |
| `batch.size` | 16KB | Максимальный размер батча |
| `compression.type` | none | `gzip`, `snappy`, `lz4`, `zstd` |
| `buffer.memory` | 32MB | Буфер памяти продюсера |

---

### 3. Consumer

#### Consumer Group и балансировка партиций
- Kafka распределяет партиции между консьюмерами группы
- При падении/добавлении консьюмера происходит **rebalance**
- Стратегии назначения: `RangeAssignor`, `RoundRobinAssignor`, `StickyAssignor`, `CooperativeStickyAssignor`
- **Cooperative rebalance** — минимизирует простой, отзывает только нужные партиции

#### Offset Management

| Режим | Описание |
|---|---|
| `enable.auto.commit=true` | Авто-коммит каждые `auto.commit.interval.ms` (риск дублей/потерь) |
| Ручной коммит | `consumer.commitSync()` / `commitAsync()` после обработки |

```properties
auto.offset.reset=earliest   # читать с начала при отсутствии offset
auto.offset.reset=latest     # читать только новые сообщения
```

#### isolation.level

| Значение | Поведение |
|---|---|
| `read_uncommitted` | Видит все сообщения включая абортированные транзакции |
| `read_committed` | Видит только сообщения из закоммиченных транзакций |

#### Consumer Lag
Разница между последним offset в партиции и текущим offset консьюмера. Мониторится через JMX или Kafka Exporter → Prometheus.

---

### 4. Delivery Guarantees

| Гарантия | Как достигается | Риск |
|---|---|---|
| **At-most-once** | Авто-коммит до обработки | Потеря сообщений |
| **At-least-once** | Ручной коммит после обработки | Дубли при сбое |
| **Exactly-once** | Idempotent producer + транзакции + `read_committed` | Сложность настройки |

Exactly-once в Spring Kafka — `KafkaTransactionManager` + `@Transactional`.

---

### 5. Topics: Partitions и Replication

```
Topic: orders (3 partitions, replication-factor=2)

Partition 0: [Leader: Broker1] [Replica: Broker2]
Partition 1: [Leader: Broker2] [Replica: Broker3]
Partition 2: [Leader: Broker3] [Replica: Broker1]
```

**Retention:**
- По времени: `retention.ms=604800000` (7 дней)
- По размеру: `retention.bytes`

**Log Compaction:** хранит только последнее значение для каждого key. Используется для state store.

---

### 6. Dead Letter Topic (DLT)

При ошибке обработки после N retry → сообщение уходит в DLT.

```java
DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
    (record, ex) -> new TopicPartition("my-topic.DLT", 0));

DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
factory.setCommonErrorHandler(handler);
```

DLT хранит оригинальные заголовки + информацию об исключении.

---

### 7. Outbox Pattern

**Проблема:** как атомарно сохранить данные в БД и отправить событие в Kafka?

**Решение:**
1. В той же транзакции сохраняем запись в таблицу `outbox_events`
2. Планировщик (polling) читает непубликованные события и шлёт в Kafka
3. Помечает как опубликованные

```
[Service] --(tx)--> [DB: business data + outbox_events]
[Scheduler] --> reads outbox_events → Kafka → marks published=true
```

Гарантирует at-least-once доставку даже при падении приложения.

---

### 8. Kafka Streams

Библиотека для stream processing поверх Kafka. Работает как обычное Java-приложение.

```java
StreamsBuilder builder = new StreamsBuilder();
KStream<String, Order> orders = builder.stream("orders");
orders
    .filter((k, v) -> v.getAmount() > 1000)
    .to("high-value-orders");
```

Возможности: `map`, `filter`, `groupBy`, `aggregate`, `join`, `windowing`.

---

## REDIS

### 1. Структуры данных

#### String
```
SET key value EX 3600    # с TTL
SETNX key value          # set if not exists (основа distributed lock)
INCR counter             # атомарный инкремент
GETSET key newvalue      # получить старое, записать новое
```

#### List (двусвязный список)
```
LPUSH list a b c         # добавить в начало
RPOP list                # извлечь с конца (очередь FIFO)
BLPOP list 30            # блокирующий pop (очереди задач)
LRANGE list 0 -1         # получить все элементы
```

#### Hash
```
HSET user:1 name "Ivan" age 30
HGET user:1 name
HGETALL user:1
HINCRBY user:1 age 1
```

#### Set (уникальные элементы)
```
SADD tags java kotlin go
SMEMBERS tags
SINTER tags1 tags2       # пересечение
SUNION tags1 tags2       # объединение
SISMEMBER tags java      # проверка членства O(1)
```

#### Sorted Set (упорядочен по score)
```
ZADD leaderboard 1500 "user:1"
ZADD leaderboard 2000 "user:2"
ZRANGE leaderboard 0 -1 WITHSCORES   # по возрастанию score
ZREVRANK leaderboard "user:1"         # место в рейтинге
```

#### Stream
```
XADD events * field1 value1 field2 value2   # добавить событие
XREAD COUNT 10 STREAMS events 0             # читать с начала
XREADGROUP GROUP mygroup consumer1 COUNT 10 STREAMS events >
```
Аналог Kafka — append-only лог с consumer groups, ACK, pending entries.

---

### 2. Persistence

| Режим | Описание | Когда использовать |
|---|---|---|
| **RDB** | Снапшот на диск по расписанию (`SAVE 900 1`) | Быстрый старт, меньше места |
| **AOF** | Логирует каждую команду (`appendonly yes`) | Минимальная потеря данных |
| **RDB + AOF** | Комбо — лучшая надёжность | Production |
| **No persistence** | Только RAM (cache) | Кэш, где потеря допустима |

AOF `appendfsync`:
- `always` — fsync на каждую запись (медленно, надёжно)
- `everysec` — fsync каждую секунду (баланс)
- `no` — fsync на усмотрение ОС (быстро, рискованно)

---

### 3. Eviction Policy

Когда Redis достигает `maxmemory`:

| Политика | Поведение |
|---|---|
| `noeviction` | Ошибка на запись (по умолчанию) |
| `allkeys-lru` | Вытесняет наименее используемые из всех ключей |
| `allkeys-lfu` | Вытесняет реже всего используемые |
| `volatile-lru` | LRU только среди ключей с TTL |
| `volatile-ttl` | Вытесняет ключи с наименьшим оставшимся TTL |
| `allkeys-random` | Случайный ключ |

Для кэша: `allkeys-lru` или `allkeys-lfu`.

---

### 4. Паттерны кэширования

#### Cache-Aside (Lazy Loading)
```
1. Читаем из кэша
2. Cache miss → читаем из БД → пишем в кэш → возвращаем
3. При записи → инвалидируем кэш (или обновляем)
```
Плюсы: простота. Минусы: cold start, stale data.

#### Write-Through
```
1. Пишем в кэш и БД одновременно (синхронно)
```
Плюсы: нет stale data. Минусы: write latency выше.

#### Write-Behind (Write-Back)
```
1. Пишем только в кэш
2. Асинхронно пишем в БД
```
Плюсы: низкая latency записи. Минусы: риск потери данных.

---

### 5. Distributed Lock

**Простой вариант:**
```
SET lock:resource uuid NX EX 30
```
- `NX` — set if not exists
- `EX 30` — автоматический TTL (защита от deadlock)
- Value = UUID клиента (для идентификации владельца)

**Освобождение (Lua script — атомарно):**
```lua
if redis.call("GET", KEYS[1]) == ARGV[1] then
    return redis.call("DEL", KEYS[1])
else
    return 0
end
```

**Redlock** — алгоритм для кластера: блокировка на большинстве (N/2+1) нод.

В Spring: `Redisson` — `RLock`, `RReadWriteLock`, `RSemaphore`.

---

### 6. Транзакции

```
MULTI         # начать транзакцию (буферизация команд)
SET k1 v1
SET k2 v2
EXEC          # выполнить все атомарно
DISCARD       # отменить
```

**WATCH** — оптимистичная блокировка:
```
WATCH balance
MULTI
DECRBY balance 100
EXEC     # вернёт nil если balance изменился после WATCH
```

Нет rollback — если одна команда в EXEC упала, остальные выполнились. Для атомарных скриптов используй Lua (`EVAL`).

---

### 7. Pub/Sub vs Streams

| | Pub/Sub | Streams |
|---|---|---|
| Persistence | Нет | Да |
| Consumer Groups | Нет | Да |
| Message ACK | Нет | Да |
| Replay | Нет | Да (по offset) |
| Use case | Realtime уведомления | Очереди задач, event log |

---

### 8. Топологии

| | Описание | Failover |
|---|---|---|
| **Standalone** | Один сервер | Нет |
| **Replica** | Master + Slaves | Ручной |
| **Sentinel** | Авто failover, не шардирует | Авто |
| **Cluster** | Шардирование + репликация (16384 слота) | Авто |

Cluster: ключ → `CRC16(key) % 16384` → slot → shard.

---

### 9. Pipelining

```java
redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
    connection.set(key1, value1);
    connection.set(key2, value2);
    return null;
});
```
Батчит команды в один TCP round-trip. Не атомарно (в отличие от MULTI/EXEC).

---

### 10. Key Design

```
user:{id}:profile        → Hash
session:{token}          → String (с TTL)
rate:{userId}:{minute}   → String INCR (с TTL)
lock:{resource}          → String NX EX
leaderboard:{gameId}     → Sorted Set
```

Разделитель `:` — стандарт. `{}` — hash tags для Cluster (гарантируют попадание ключей на один shard).
