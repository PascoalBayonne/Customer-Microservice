# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 4 (Spring Framework 7) microservice for customer management, demonstrating the **Event-Carried State Transfer (ECST)** pattern with a transactional outbox via **Spring Modulith**. Events carry full customer state (not just IDs) and are externalized to Kafka.

## Build & Test Commands

```bash
# Build (skip tests)
./mvnw clean package -DskipTests

# Run all tests (requires Docker for Testcontainers)
./mvnw test

# Run a single test class
./mvnw test -Dtest=CustomerControllerTest

# Run a single test method
./mvnw test -Dtest=CustomerControllerTest#shouldCreateCustomer

# Run Spring Cloud Contract verification tests
./mvnw test -Dtest=*ITCase

# Start the application (requires MySQL on localhost:3306 and Kafka)
./mvnw spring-boot:run
```

## Infrastructure Requirements

- **Java 25**
- **Docker** required for tests (Testcontainers spins up MySQL 8.0.24 + Confluent Kafka)
- **Local dev**: `docker-compose-complete.yml` provides the full stack (Kafka, Zookeeper, Schema Registry, MySQL, Conduktor UI, MinIO, MongoDB, PostgreSQL)
- App runs on port **8283**, MySQL database `Customer` (root/root)

## Architecture

### Layered Package Structure (`pt.bayonne.sensei.customer`)

- **`controller`** — REST API layer. Endpoints defined in `API.java` constants (`/api/v1/customer`). Uses MapStruct (`CustomerMapper`) for DTO-to-domain conversion. Request validation via `@Valid`.
- **`service`** — Business logic. `CustomerServiceImpl` orchestrates persistence and event publishing.
- **`domain`** — JPA entities and **value objects** (`FirstName`, `LastName`, `BirthDate`, `EmailAddress`, `SSN`). The `Customer` entity uses a factory method `Customer.create(...)` and wraps all fields in value objects.
- **`messaging`** — Spring Modulith module (declared `OPEN` in `package-info.java`). Contains event types, Kafka publishing config, and the outbox listener.
- **`repository`** — Spring Data JPA repositories.
- **`config`** — ShedLock configuration for distributed lock on scheduled tasks.

### Two Event Publishing Paths

1. **`create()` path**: Uses Spring's `ApplicationEventPublisher` to publish a `CustomerDTO` (messaging) record annotated with `@Externalized`. Spring Modulith intercepts this, stores it in the `event_publication` table (transactional outbox), and asynchronously externalizes it to Kafka topic `customer-topic`.
2. **`changeEmail()` path**: Pushes a `CustomerEvent.EmailChanged` message directly onto a Reactor `Sinks.Many<Message<?>>` bound to Spring Cloud Stream's `customerSupplier-out-0` function, bypassing the Modulith outbox.

### Key Integration Points

- **Spring Modulith** handles outbox persistence (`spring-modulith-starter-jdbc`) and Kafka externalization (`spring-modulith-events-kafka`). Events auto-republish on restart.
- **Spring Cloud Stream** with Kafka binder — function binding `customerSupplier-out-0` maps to topic `customer-topic`.
- **ShedLock** prevents concurrent scheduled task execution across instances (uses JDBC provider with 30s default lock).
- **Spring Cloud Contract** — contract tests use base class in `pt.bayonne.sensei.customer.contractsss` package, test suffix `ITCase`.

### Testing

- Tests use **Testcontainers** with reuse enabled (`testcontainers.reuse.enable=true`). `ContainersConfiguration` defines MySQL and Kafka containers with `@ServiceConnection`.
- Test profile (`application-test.properties`): random port, `ddl-auto=create`.
- Contract verification base class: `ShouldPublishCustomerCreatedBase`.

## Key Libraries

- **Lombok** + **MapStruct** (with lombok-mapstruct-binding for annotation processor ordering)
- **Jackson** for JSON serialization
- **ShedLock** for distributed scheduling locks
- **Testcontainers** for integration tests
