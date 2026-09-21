# Design Review: customer

Reviewed: 2026-07-22 · local working copy (branch `feature/ECST`) · 24 Java files, 1 module

## Summary

This is a small Spring Boot microservice for managing customers, built to demonstrate the **Event-Carried State Transfer** pattern — when a customer is created or changed, the service publishes an event carrying the customer's full data to Kafka so other systems can react. The domain modeling here is genuinely good for its size: business concepts like email, SSN, and birth date are their own small self-validating types (not bare strings), the `Customer` object has no setters, and it is built through a factory method. That puts it well ahead of the typical "everything is a getter/setter bag" Spring app.

The problems are concentrated in three places: a **data-loss bug in how SSN is modeled** (stored as an `Integer`, which silently drops leading zeros), an **inconsistent and less-reliable second path for publishing events** (the email-change path bypasses the transactional outbox and can lose messages), and **duplicated/dead plumbing** (two different mapper classes, two classes both named `CustomerDTO`, and an entire hand-rolled outbox that nothing uses). None of these require a rewrite. Overall health: **solid foundation, needs targeted attention.**

## How the project is organized today

Packages are named after **technical roles** (controller, service, domain, repository, messaging) rather than business areas — normal and fine for a single-purpose service this small.

```
pt.bayonne.sensei.customer
├── controller        REST layer
│   ├── dto           CustomerDTO (request+response), EmailDTO, ResourceNotFoundException
│   └── mapper        CustomerMapper (MapStruct)
├── service           CustomerService + CustomerServiceImpl
├── domain            Customer (@Entity), value objects, OutboxMessage (@Entity)
├── messaging         Modulith module: events, Kafka config, outbox listener
│   └── event         CustomerDTO (@Externalized), CustomerEvent
├── repository        CustomerRepository, OutboxMessageRepository
└── config            ShedLock + Jackson config
```

## What's working well

- **Real value objects with validation at construction** (`FirstName`, `LastName`, `BirthDate`, `EmailAddress`, `SSN` in `domain/`). Each has a private constructor and a static factory that rejects invalid input, so an invalid email or a future birth date is *impossible to construct*. This is exactly the right instinct and the strongest part of the codebase.
- **`Customer` is not an anemic data bag** (`domain/Customer.java`). No public setters, built via `Customer.create(...)`, and state changes go through an intention-revealing method (`changeEmail(...)`) rather than a raw setter. Behaviour lives with the data.
- **Thin controller** (`controller/CustomerController.java`). Endpoints validate, call one service method, and map the result — no business rules leaking into the web layer.
- **Proper transactional outbox for creation** via Spring Modulith. The `create()` path stores the event in the same database transaction as the customer, so the event can't be lost if Kafka is briefly down.

## Issues found

> **Implementation status (updated 2026-07-22, branch `feature/ECST`):** ✅ **All issues resolved.**
> Every item below is implemented and the full test suite passes (`mvn test` — 15/15 green,
> including the Spring Cloud Contract `ITCase` that verifies publishing to `customer-topic`).
> Note: use system Maven 3.9+ (`mvn`), not the bundled `./mvnw` 3.8.6 — the contract plugin
> requires Maven 3.9. See the per-item ✅ notes for what changed.

### 1. ✅ SSN is stored as a number, which silently corrupts real values — High

- **What we found:** `SSN` wraps an `Integer` (`domain/SSN.java`), and validity is checked with `String.valueOf(ssn).toCharArray().length == 9`. A US Social Security Number is a 9-*digit string*, not a quantity. Any SSN beginning with `0` (e.g. `012-34-5678`) becomes the integer `12345678`, whose string length is 8 — so it both **loses the leading zero** and then **fails its own validation**. Numbers also invite meaningless operations (you can add two SSNs). This is *primitive obsession* — using a general-purpose type (`Integer`) for a concept that has its own rules.
- **Where:** `domain/SSN.java`, plus `Integer ssn` threaded through `controller/dto/CustomerDTO.java`, `messaging/event/CustomerDTO.java`, and both mappers.
- **Why it matters:** this is a correctness bug, not a style issue. A valid SSN starting with zero — roughly 1 in 10 — is either rejected or stored wrong, and the wrong value is then broadcast to every downstream consumer over Kafka.
- **What to do instead:** store the SSN as a validated `String`.
  ```java
  // before
  private Integer ssn;
  Assert.isTrue(String.valueOf(ssn).length() == 9, "...");

  // after
  private String value;                       // "012345678"
  Assert.isTrue(value.matches("\\d{9}"), "SSN must be exactly 9 digits");
  ```
- **✅ Done:** `domain/SSN.java` already stored a validated 9-digit `String` (`ssn.matches("\\d{9}")`);
  both DTOs and the MapStruct mapper use `String`. Tidied the DB layer to match: `schema/schema.sql`
  `ssn` → `varchar(9)`, quoted the SSN literals in `schema/data.sql` + `src/test/resources/db/data.sql`,
  and quoted `expectations/get-customer-success.json`. Added an `SSNTest` leading-zero round-trip test.

### 2. ✅ The email-change path can lose events and isn't transactional — High

- **What we found:** `create()` publishes through Spring Modulith's transactional outbox (reliable). But `changeEmail()` (`service/CustomerServiceImpl.java:46`) does something different: it pushes the event straight onto an in-memory `Sinks.Many` (`customerProducer`) configured as `replay().latest()`, and the method has **no `@Transactional` annotation**. So the two event flows use two unrelated mechanisms, and the email flow has neither the database-backed guarantee nor a transaction wrapping the save-and-publish.
- **Where:** `service/CustomerServiceImpl.java:46-68`; sink defined in `messaging/CustomerMessaging.java`.
- **Why it matters:** if the app restarts (or the Kafka binder is momentarily unavailable) after the DB commit but before the in-memory sink is drained, the `EmailChanged` event is gone forever — the customer's email is updated in the database but no downstream system ever hears about it. That is the exact failure the outbox on the `create()` path was built to prevent. Two mechanisms for the same job also doubles the surface area anyone maintaining this has to understand.
- **What to do instead:** route `EmailChanged` through the same Modulith outbox as creation — publish a domain event with `applicationEventPublisher.publishEvent(...)` inside a `@Transactional` method and let an `@Externalized` event carry it to Kafka, deleting the bespoke sink path.
- **✅ Done:** `changeEmail` is now `@Transactional` and publishes the same `@Externalized`
  `CustomerStateChanged` (full ECST state) as `create`, via `applicationEventPublisher`. Retired the
  in-memory `Sinks.Many`/`customerSupplier` (deleted `messaging/CustomerMessaging.java` and the
  `spring.cloud.function.definition` + stream bindings from both properties files). One unified
  publishing path remains.

### 3. ✅ A whole hand-rolled outbox exists but nothing uses it — Medium

- **What we found:** `domain/OutboxMessage.java` (a full JPA entity with `eventType`, `payload`, `sent`, a `delivered()` method) and `repository/OutboxMessageRepository.java` (with `findTop10BySentOrderByIdAsc`) implement a manual transactional-outbox pattern. A grep of the whole source tree shows **no code writes, reads, or schedules them** — the actual outbox is Modulith's `event_publication` table.
- **Where:** `domain/OutboxMessage.java`, `repository/OutboxMessageRepository.java`.
- **Why it matters:** dead code that *looks* load-bearing is a trap. A newcomer reasonably assumes this is how events are delivered, wires new work into it, and ships something that silently never fires. It also muddies the domain package with a persistence concern that isn't a business concept.
- **What to do instead:** delete both files. If a manual outbox is wanted later, it's a deliberate choice — not leftover scaffolding.
- **✅ Done:** deleted `domain/OutboxMessage.java` and `repository/OutboxMessageRepository.java`, and
  removed the `outbox_message` / `outbox_message_seq` tables from `schema/schema.sql`. (Note:
  `messaging/OutboxMessagePublisher` is the *live* Modulith listener and was kept.)

### 4. ✅ Two classes named `CustomerDTO`, and one of them is request and response at once — Medium

- **What we found:** there are two unrelated `CustomerDTO` types — `controller/dto/CustomerDTO.java` (the REST shape) and `messaging/event/CustomerDTO.java` (the Kafka event shape). Separately, the controller uses the *same* `controller.dto.CustomerDTO` as both the incoming `@RequestBody` for creation **and** the outgoing response body (`controller/CustomerController.java:33,55`).
- **Where:** the two `CustomerDTO` files; `CustomerController.create` and `findByCustomerId`.
- **Why it matters:** identical names for different concepts force every reader (and every import line) to disambiguate, and mistakes compile silently. Using one type for request and response means the created-customer response advertises fields a client must *send* (like `ssn`) and can't carry fields it should *return* (like the generated `id`) — the two directions have genuinely different shapes and will drift apart.
- **What to do instead:** rename by role — `CreateCustomerRequest` / `CustomerResponse` in the web layer, and something like `CustomerStateChanged` for the event payload. Records make this cheap.
- **✅ Done:** web `CustomerDTO` split into `controller/dto/CreateCustomerRequest` (validated request)
  and `controller/dto/CustomerResponse` (now includes generated `id`); controller + MapStruct mapper
  (`mapToCustomer` / `mapToCustomerResponse`) updated. Event payload renamed to `CustomerStateChanged`
  (issue 7).

### 5. ✅ "Customer not found" behaves differently depending on the endpoint — Medium

- **What we found:** `findByCustomerId` throws `ResourceNotFoundException` (mapped to HTTP 404), but `changeEmail` throws a plain `IllegalArgumentException` for the identical "no such customer" condition (`service/CustomerServiceImpl.java:54`).
- **Where:** `service/CustomerServiceImpl.java` — `changeEmail` vs `findByCustomerId`.
- **Why it matters:** the same real-world situation returns two different HTTP statuses — a `PATCH` to a missing customer surfaces as a 500 Internal Server Error instead of a 404, which misleads clients and monitoring into treating a normal "not found" as a server fault.
- **What to do instead:** throw `ResourceNotFoundException` from both. Consider giving it a home outside `controller/dto` (see below), since the service layer now depends on it.
- **✅ Done:** `changeEmail` now throws `ResourceNotFoundException` (was `IllegalArgumentException`), so
  a `PATCH` to a missing customer returns 404 like the GET.

### 6. ✅ Two mappers doing the same job; the web layer owns a "not found" exception — Low

- **What we found:** MapStruct's `controller/mapper/CustomerMapper.java` maps `Customer ↔ CustomerDTO`, while `CustomerServiceImpl` declares its *own* private inner `CustomerMapper` interface (`service/CustomerServiceImpl.java:81`) that flattens `Customer` into the messaging `CustomerDTO`. Two hand-maintained mappings pull data out of the same value objects. Separately, `ResourceNotFoundException` — a service-level concept — lives in `controller/dto`.
- **Where:** `controller/mapper/CustomerMapper.java`, inner mapper in `service/CustomerServiceImpl.java`, `controller/dto/ResourceNotFoundException.java`.
- **Why it matters:** low severity but real friction — add a field to `Customer` and you must remember to update two mappers in different styles, and the misplaced exception blurs which layer owns error semantics.
- **What to do instead:** consolidate the event mapping (e.g. a factory on the event record: `CustomerStateChanged.from(customer)`), and move `ResourceNotFoundException` to the service or a shared package.
- **✅ Done:** deleted the private inner `CustomerMapper` in `CustomerServiceImpl`; event mapping is now
  the `CustomerStateChanged.from(Customer)` factory. `ResourceNotFoundException` moved to the new
  shared package `pt.bayonne.sensei.customer.exception`.

## Improvement plan

### Phase 1 — Quick wins (days)
| # | ✓ | Change | Files | Effort | How to verify |
|---|---|--------|-------|--------|---------------|
| 1 | ✅ | Delete the unused hand-rolled outbox | `domain/OutboxMessage.java`, `repository/OutboxMessageRepository.java` | S | `mvn test` stays green; app still starts and creation event still reaches Kafka |
| 2 | ✅ | Make `changeEmail` throw `ResourceNotFoundException` for missing customer | `service/CustomerServiceImpl.java` | S | New test: `PATCH .../email` on unknown id returns 404, not 500 |
| 3 | ✅ | Move `ResourceNotFoundException` out of `controller/dto` → `exception/` | `exception/ResourceNotFoundException.java` + imports | S | Compile + existing tests pass |
| 4 | ✅ | Rename the web DTO by role and split request vs response | `controller/dto/CreateCustomerRequest.java`, `controller/dto/CustomerResponse.java`, `CustomerController`, `CustomerMapper` | M | Controller test asserts response includes `id` and omits nothing sensitive |

### Phase 2 — Strengthen the model (weeks)
| # | ✓ | Change | Files | Effort | How to verify |
|---|---|--------|-------|--------|---------------|
| 5 | ✅ | Fix SSN: store as validated 9-digit `String` (+ DB column / seeds) | `domain/SSN.java`, both DTOs, mapper, `schema/*.sql`, `db/data.sql`, `SSNTest` | M | Unit test: SSN `"012345678"` is accepted and round-trips intact; non-9-digit rejected |
| 6 | ✅ | Consolidate the two mappers into one place | `service/CustomerServiceImpl.java`, `messaging/event/CustomerStateChanged.java` | M | Existing mapping tests pass; only one mapping definition remains |
| 7 | ✅ | Rename the second `CustomerDTO` (event payload) → `CustomerStateChanged` | `messaging/event/CustomerStateChanged.java` + refs | S | Compile; contract/`ITCase` tests pass |

### Phase 3 — Boundaries (ongoing)
| # | ✓ | Change | Files | Effort | How to verify |
|---|---|--------|-------|--------|---------------|
| 8 | ✅ | Route `EmailChanged` through the Modulith outbox; make `changeEmail` `@Transactional`; retire the in-memory sink | `service/CustomerServiceImpl.java`, `messaging/CustomerMessaging.java` (deleted), `messaging/event/CustomerEvent.java` (deleted) | L | Contract `ITCase` publishes to `customer-topic`; one unified publishing path remains |
| 9 | ✅ | Consider one Modulith module per concern if the service grows | package layout | L | **Intentionally deferred — kept as-is (service still small), per the review's own guidance.** No code change. |

## Suggested first pull request

**Delete the dead hand-rolled outbox (Phase 1, item 1).** It's the single safest, highest-clarity change: `OutboxMessage` and `OutboxMessageRepository` are provably unreferenced (a full-tree grep finds no writer, reader, or scheduler), so removing them can't change behaviour — and it immediately stops the next engineer from mistaking them for the real event-delivery path. Write the test *first* as a safety net: a small integration test that creates a customer and asserts a `customer-topic` message is produced, proving the genuine Modulith outbox — not the deleted code — is what delivers events. With that green before and after the deletion, the PR is obviously correct and sets up the more substantial SSN and event-path fixes that follow.