# Customer Service - Transactional Outbox Pattern Architecture

## Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         CLIENT (REST API)                                    │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │ POST /api/customers
                                 ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                    CUSTOMER SERVICE (Spring Boot)                            │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐    │
│  │                      API LAYER                                      │    │
│  │                                                                     │    │
│  │  ┌──────────────────────────────────────────────────────────┐     │    │
│  │  │  CustomerController                                       │     │    │
│  │  │  - POST   /api/customers                                  │     │    │
│  │  │  - PUT    /api/customers/{id}                            │     │    │
│  │  │  - DELETE /api/customers/{id}                            │     │    │
│  │  │  - GET    /api/customers/{id}                            │     │    │
│  │  └──────────────────────────────────────────────────────────┘     │    │
│  └────────────────────────────┬───────────────────────────────────────┘    │
│                                │                                             │
│  ┌────────────────────────────▼───────────────────────────────────────┐    │
│  │                      DOMAIN LAYER                                   │    │
│  │                                                                     │    │
│  │  ┌──────────────────────────────────────────────────────────┐     │    │
│  │  │  CustomerService                                          │     │    │
│  │  │                                                           │     │    │
│  │  │  @Transactional                                          │     │    │
│  │  │  createCustomer(command) {                               │     │    │
│  │  │    1. Validate business rules                            │     │    │
│  │  │    2. Save customer to DB ────────┐                      │     │    │
│  │  │    3. Publish domain event        │                      │     │    │
│  │  │       eventPublisher.publishEvent()                      │     │    │
│  │  │  }                                 │                      │     │    │
│  │  └────────────────────────────────────┼──────────────────────┘     │    │
│  └────────────────────────────────────────┼──────────────────────────┘    │
│                                           │                                 │
│            ┌──────────────────────────────┴──────────────────┐            │
│            │      SAME DATABASE TRANSACTION                  │            │
│            │                                                  │            │
│  ┌─────────▼────────────────────────┐  ┌──────────────────▼──────────┐   │
│  │   PostgreSQL - customers Table   │  │  Spring Modulith Outbox     │   │
│  │                                   │  │  event_publication Table     │   │
│  │  id  | firstName | lastName | .. │  │                              │   │
│  │  ──────────────────────────────  │  │  id | event_type | payload  │   │
│  │  1   | John      | Doe      | .. │  │  1  | CREATED    | {...}    │   │
│  └───────────────────────────────────┘  └──────────────┬───────────────┘   │
│                                                         │                   │
│                        ✓ COMMIT ────────────────────────┘                   │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                    MESSAGING LAYER                                    │  │
│  │                                                                       │  │
│  │  ┌─────────────────────────────────────────────────────────────┐    │  │
│  │  │  CustomerEventExternalizer                                   │    │  │
│  │  │  (Async - Separate Thread)                                   │    │  │
│  │  │                                                               │    │  │
│  │  │  @EventListener                                              │    │  │
│  │  │  onCustomerCreated(event) {                                  │    │  │
│  │  │    1. Read from outbox table                                 │    │  │
│  │  │    2. Convert to Avro format                                 │    │  │
│  │  │    3. Pass to Kafka publisher                                │    │  │
│  │  │  }                                                            │    │  │
│  │  └─────────────────────────┬───────────────────────────────────┘    │  │
│  │                            │                                         │  │
│  │  ┌─────────────────────────▼───────────────────────────────────┐    │  │
│  │  │  KafkaCustomerEventPublisher                                 │    │  │
│  │  │  (Spring Cloud Stream)                                       │    │  │
│  │  │                                                               │    │  │
│  │  │  publish(avroEvent) {                                        │    │  │
│  │  │    streamBridge.send("customerEventPublisher-out-0", event) │    │  │
│  │  │  }                                                            │    │  │
│  │  └─────────────────────────┬───────────────────────────────────┘    │  │
│  └────────────────────────────┼──────────────────────────────────────┘  │
└─────────────────────────────────┼────────────────────────────────────────┘
                                  │
                                  │ Avro Serialized Event
                                  ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                          KAFKA INFRASTRUCTURE                                │
│                                                                              │
│  ┌─────────────────────────┐          ┌──────────────────────────────┐     │
│  │   Schema Registry       │          │      Kafka Broker             │     │
│  │   (Port 8081)           │          │      (Port 9092)              │     │
│  │                         │◄─────────┤                               │     │
│  │  - Validates schema     │  Store   │  Topic: customer-events       │     │
│  │  - Stores Avro schemas  │  Schema  │                               │     │
│  │  - Returns schema ID    │          │  Partition 0: [msg1, msg2...] │     │
│  └─────────────────────────┘          │  Partition 1: [msg3, msg4...] │     │
│                                        │  Partition 2: [msg5, msg6...] │     │
│                                        └───────────────┬───────────────┘     │
└────────────────────────────────────────────────────────┼─────────────────────┘
                                                         │
                                    Consumers can read   │
                                    events reliably      ↓
                                              ┌─────────────────────┐
                                              │  Other Services     │
                                              │  (Event Consumers)  │
                                              └─────────────────────┘
```

## Key Components

### 1. **Domain Events (Internal)**
```java
@Externalized("customer-events::customer.created.#{customerId()}")
public record CustomerCreatedEvent(CustomerDTO customer, Instant occurredOn)
```
- Annotated with `@Externalized` to mark for outbox processing
- SpEL expression defines topic and routing key

### 2. **Spring Modulith Outbox**
- Automatically intercepts domain events
- Stores them in `event_publication` table
- Part of the same database transaction
- Guarantees atomicity

### 3. **Event Externalization**
```java
@EventListener
public void onCustomerCreated(CustomerCreatedEvent event) {
    CustomerEvent avroEvent = buildAvroEvent(...);
    kafkaPublisher.publish(avroEvent);
}
```
- Listens to externalized events
- Converts to Avro format
- Delegates to Kafka publisher

### 4. **Kafka Publisher**
```java
streamBridge.send("customerEventPublisher-out-0", message);
```
- Uses Spring Cloud Stream's StreamBridge
- Handles Avro serialization
- Sends to configured Kafka topic

## Event Flow Timeline

```
Time →

User    Service         Database          Outbox           Kafka
 │          │               │               │               │
 │  POST    │               │               │               │
 ├─────────>│               │               │               │
 │          │               │               │               │
 │          │ BEGIN TX      │               │               │
 │          ├──────────────>│               │               │
 │          │               │               │               │
 │          │ INSERT        │               │               │
 │          │ Customer      │               │               │
 │          ├──────────────>│               │               │
 │          │               │               │               │
 │          │ Publish Event │               │               │
 │          │ (Internal)    │               │               │
 │          │               │               │               │
 │          │         INSERT Event          │               │
 │          ├──────────────────────────────>│               │
 │          │               │               │               │
 │          │ COMMIT        │               │               │
 │          ├──────────────>│               │               │
 │          │               │               │               │
 │  200 OK  │               │               │               │
 │<─────────┤               │               │               │
 │          │               │               │               │
 │          │        [Async Process]        │               │
 │          │               │               │               │
 │          │               │  Read Event   │               │
 │          │               │<──────────────┤               │
 │          │               │               │               │
 │          │               │  Convert to   │               │
 │          │               │  Avro         │               │
 │          │               │               │               │
 │          │               │          Publish              │
 │          │               │          Event                │
 │          │               │───────────────────────────────>│
 │          │               │               │               │
 │          │               │  Mark Complete│               │
 │          │               │<──────────────┤               │
 │          │               │               │               │
```

## Failure Scenarios

### Scenario 1: Kafka is Down During Event Publishing

```
✓ Customer saved to database
✓ Event saved to outbox table
✓ Transaction commits
✗ Kafka publish fails
→ Event remains in outbox
→ Automatic retry when Kafka is available
→ Eventually published successfully
```

### Scenario 2: Application Crashes After Commit

```
✓ Customer saved
✓ Event saved to outbox
✓ Transaction commits
✗ Application crashes before publishing
→ On restart, Spring Modulith reads unpublished events
→ Events are published automatically
```

### Scenario 3: Database Transaction Fails

```
✗ Customer save fails (e.g., constraint violation)
✗ Transaction rolls back
✗ Event is NOT saved to outbox
→ No event published (correct behavior)
→ No inconsistency between database and Kafka
```

## Benefits of This Implementation

1. **Guaranteed Event Delivery**: Events are stored in the database before publishing
2. **Exactly-Once Semantics**: Each event is published exactly once
3. **No Message Loss**: Even if Kafka is down, events are persisted
4. **Transactional Consistency**: Database and events are always in sync
5. **Schema Evolution**: Avro provides backward/forward compatibility
6. **Automatic Retry**: Spring Modulith handles retries automatically
7. **Idempotency**: Same event won't be published multiple times

## Configuration Highlights

### Spring Modulith
```yaml
spring:
  modulith:
    events:
      externalization:
        enabled: true
      jdbc:
        schema-initialization:
          enabled: true
```

### Kafka with Avro
```yaml
spring:
  cloud:
    stream:
      kafka:
        binder:
          configuration:
            value.serializer: io.confluent.kafka.serializers.KafkaAvroSerializer
            schema.registry.url: http://localhost:8081
      bindings:
        customerEventPublisher-out-0:
          destination: customer-events
          content-type: application/*+avro
```

This architecture ensures reliable, ordered, and guaranteed delivery of events from your Customer microservice to Kafka!
