# Event-Carried State Transfer with Spring Modulith & Kafka

## 1. Recap: Our Previous Implementation

### 1.1 The Problem We Were Solving
In our previous video, we tackled the **dual-writes problem**.  
This happens when an application:
- Persists state changes to a database
- Then publishes an event to Kafka using Spring Cloud Stream

If one operation succeeds and the other fails, the system becomes inconsistent.  
According to the **Transactional Outbox Pattern**, both actions must be part of the same logical unit of work.

---

### 1.2 The Transactional Outbox Pattern (Hand-Rolled)
We implemented the **Transactional Outbox Pattern** manually:
- Write the event to an outbox table in the same transaction
- Later publish it to Kafka

While correct, this approach required:
- Custom scheduling
- Retry logic
- Cleanup strategies
- Error handling

All of this had to be built and maintained by us.

---

### 1.3 The Real-World Cost
In real business projects:
- This solution easily consumes **multiple story points**
- Requires careful design and testing
- Or very precise AI prompts… otherwise you’ll feel the **tokens vanishing**

This is not free architecture.

---

### 1.4 What We’ll Do Now
This time, we’re not just improving the solution.

We’ll use a **straightforward, opinionated framework** to implement:
- **Event-Carried State Transfer**
- **Transactional Outbox**
- **Apache Kafka integration**

Using **Spring Modulith**.

The result:
- Less boilerplate
- Clear module boundaries
- Perfect fit for **Clean Architecture** and **Hexagonal Architecture**

---

## 2. Introducing Spring Modulith

### 2.1 What Is Spring Modulith
If you’re a Java developer using Spring Boot and **Domain-Driven Design**, you should look at Spring Modulith.

According to the official documentation:
> Spring Modulith is an opinionated toolkit to build **domain-driven, modular applications** with Spring Boot.

It provides:
- A functional, package-based modular structure
- Explicit boundaries between application modules
- Controlled interaction via application events

In short: structure, discipline, and architectural clarity.

---

### 2.2 Dependencies Overview
To implement Event-Carried State Transfer with Kafka:
- We start by importing the **spring-modulith-bom**
- Then add **spring-modulith-starter-core**

Why `starter-core`?
Because it provides:
- Module detection
- Application event infrastructure
- Module interaction enforcement

This is the foundation of Spring Modulith.

---

## 3. Working with Application Events

### 3.1 Publishing Application Events & Dependency Inversion
Here we apply the **Dependency Inversion Principle**.

Instead of:
- One module directly calling another

We:
- Publish an **application or domain event**

Spring Modulith uses Spring’s `ApplicationEventPublisher` abstraction.

Motivation and advantages:
- Publishers do not depend on consumers
- No compile-time coupling
- Modules communicate via events, not method calls
- Business logic remains isolated

Important distinction from the official docs:
- Spring has **framework events** (e.g. `ContextRefreshedEvent`)
- We are using **application-specific domain events**

---

### 3.2 Listening to Events: `@EventListener` vs `@ApplicationModuleListener`

Initially, we used `@EventListener`.

According to Spring documentation:
- `@EventListener` listens to any application event
- It has no awareness of module boundaries

Then we switched to `@ApplicationModuleListener`.

Why this matters:
- `@ApplicationModuleListener` is **module-aware**
- It makes cross-module communication explicit
- It enforces architectural boundaries defined by Spring Modulith

From a Dependency Inversion perspective:
- Modules depend on **events**, not implementations
- The listener declares its dependency explicitly at the module level

This turns events into **architectural contracts**, not just callbacks.

---

## 4. Event Externalization with Spring Modulith

### 4.1 The Naive Approach
A naive solution would be:
- Listen to an application event
- Manually publish it using KafkaTemplate or Spring Cloud Stream

But this:
- Reintroduces boilerplate
- Couples event handling with messaging concerns
- Repeats outbox logic

Instead, Spring Modulith does this automatically.

---

### 4.2 Enabling Event Externalization
We add the dependency:
- **spring-modulith-events-api**

And enable:
- `spring.modulith.events.externalization.enabled=true`

According to the documentation, this tells Spring Modulith:
> Certain application events are meant to leave the application boundary.

---

### 4.2.1 Choosing Apache Kafka
To use Kafka as the external messaging system:
- We add **spring-modulith-events-kafka**

This integrates Kafka directly into the Modulith event externalization mechanism.

---

### 4.2.2 Marking Events with `@Externalized`
We annotate the event with `@Externalized`.

Meaning:
- This event is still an internal application event
- But it is also eligible for external publication
- Spring Modulith handles persistence and dispatch automatically

This is declarative, not imperative.

---

### 4.2.3 JSON Serialization
We enable:
- `spring.modulith.events.kafka.enable-json=true`

Why?
- Simple, human-readable payloads
- Easy debugging
- No schema registry required initially
- Faster onboarding and iteration

---

## 5. Event Publication Registry (The Outbox)

Spring Modulith internally maintains an **Event Publication Registry**.

This acts as:
- The **outbox table**
- The **event log**
- The persistence backbone of the transactional outbox pattern

Failed events can be automatically re-published using:
- `spring.modulith.events.republish-outstanding-events-on-restart`

---

### 5.1 Persisting Events with JPA
We add:
- **spring-modulith-starter-jpa**

And enable:
- `spring.modulith.events.jdbc.schema-initialization.enabled=true`

Why?
- Spring Modulith needs database tables to store event publications
- Schema initialization guarantees consistency across environments

---

### 5.2 When Events Are Marked as Completed
According to the documentation:
- An event publication is marked as completed when
    - The surrounding transaction commits successfully
    - And the `@ApplicationModuleListener` finishes without errors

This guarantees:
- Reliability
- Safe retries
- No lost events

You’ll clearly see this lifecycle in the application logs.

---

### 5.3 Event Completion Modes
Spring Modulith supports different completion strategies via:
- `spring.modulith.events.completion-mode`

Available modes:
- **UPDATE** – mark the event as completed
- **DELETE** – remove it after publication
- **ARCHIVE** – retain it for auditing or compliance

Choose based on:
- Observability needs
- Retention policies
- Regulatory requirements

---

## Final Takeaway
With Spring Modulith:
- Event-Carried State Transfer becomes declarative
- Transactional Outbox is built-in
- Kafka integration is seamless
- Architectural boundaries are enforced

Less boilerplate.  
More correctness.  
Production-ready event-driven design.
