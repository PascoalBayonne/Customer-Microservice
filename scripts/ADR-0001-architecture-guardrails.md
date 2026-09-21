# ADR-0001: ArchUnit fitness functions for domain purity, thin controllers, and scoped Lombok

**Status:** Proposed
**Date:** 2026-09-21
**Deciders:** Engineering core team · tech leads of each service team
**Applies to:** every Java module in the organization with a `src/main/java`

---

## Context

Our design standard is a **rich domain model**: business rules live in the objects that own the
data, the domain compiles without a framework on the classpath, and adapters at the edge translate
to HTTP, JPA and Kafka. That standard is written down, repeated in reviews, and taught in
onboarding.

It still drifts, in the same three directions, in every codebase we own:

1. **Anemic domain models.** Entities become bags of getters and setters; the rules move into
   `SomethingService`, where they are duplicated into the next service that needs them.
2. **Logic in controllers.** The web adapter reaches for the repository or the entity directly,
   so the rule is now reachable only over HTTP and testable only with a running context.
3. **Lombok everywhere.** `@Data` on a JPA entity generates the setters that make item 1 easy and
   the `equals`/`hashCode`/`toString` that make Hibernate behave unpredictably.

Three forces make this urgent now:

- **Review does not scale and does not hold.** A reviewer who blocks `@Data` on Monday approves it
  on Friday. A rule enforced by attention is a rule that decays.
- **A large and growing share of our code is written by coding agents.** Agents mirror the
  conventions already present in the repository. Point one at a module with an anemic slice and it
  will produce a second anemic slice, faster than review can reject it. Instructions in
  `CLAUDE.md`, `AGENTS.md` or a skill are *advice* the model may or may not follow; only a failing
  build is a *guarantee*.
- **Java 21+ removed most of the original justification for Lombok.** Records, `var` and compact
  constructors cover the carrier and boilerplate cases that Lombok was adopted for.

We already own most of the machinery: the `arch-guardrails` plugin ships the mandatory Lombok
fitness function, a template, and a write-time hook. What is missing is the decision that makes it
organization-wide, the rules covering the other two drifts, and a written Lombok policy that says
where the tool is still welcome.

---

## Decision

**We enforce our architecture with ArchUnit fitness functions that run in every module's build,
and we scope Lombok by layer instead of banning or allowing it globally.**

Concretely:

1. Every module with a `src/main/java` carries `architecture/DomainPurityTest.java` (mandatory,
   already specified by the `arch-guardrails` plugin) plus the three companion rule groups below.
2. **Lombok is blocked in the domain layer**, by four independent checks, because the domain is the
   code we intend to still own in ten years.
3. **Lombok is allowed outside the domain, by allowlist** — `@RequiredArgsConstructor`, `@Slf4j`,
   `@Builder`, `@UtilityClass` — and forbidden everywhere in two cases: `@SneakyThrows`, and
   `@Data`/`@EqualsAndHashCode` on a JPA entity.
4. Violations are fixed, or frozen with `FreezingArchRule` under a named owner and a date. Never
   silenced with `@ArchIgnore`, a widened package matcher, or a deleted test.
5. The rules ship as a plugin, not as a wiki page, so a new module inherits them instead of
   re-deciding them.

Every rule quoted in this ADR was compiled and run against a Spring Boot 4.1.1 / Java 25 module
before publication, and each was confirmed to fail on code that violates it.

---

## Options considered

### Option A: Written guidelines plus code review (status quo)

| Dimension | Assessment |
|---|---|
| Complexity | Low — nothing to build |
| Cost | High and recurring — paid in reviewer attention, forever |
| Scalability | Poor — degrades with team size, tight deadlines, and agent-written volume |
| Team familiarity | Total |

**Pros:** no tooling, no false positives, full human judgement on every case.
**Cons:** not reproducible; decays under deadline pressure; cannot keep up with the volume of
agent-written code; the same discussion is re-litigated in every PR; new joiners learn the rule
only by breaking it.

### Option B: ArchUnit fitness functions in the build *(chosen)*

| Dimension | Assessment |
|---|---|
| Complexity | Low — one test dependency, one test class per module |
| Cost | One-off install, plus tuning per module; a few minutes of build time |
| Scalability | Excellent — identical verdict on every module, every commit, every author, human or not |
| Team familiarity | Moderate — ArchUnit's fluent API reads like English; the custom conditions need one reviewer who knows them |

**Pros:** the rule is executable and versioned next to the code it governs; the failure message
names the exact member; it works identically in the IDE, the CLI and CI; `FreezingArchRule` gives a
migration path for legacy modules; it constrains coding agents without depending on their
cooperation.
**Cons:** bytecode-based, so `SOURCE`-retention annotations are invisible without a source scan
(see the Lombok detection note); heuristics such as "this class has no behaviour" produce false
positives on legitimate carrier types; a rule that is wrong is now wrong in every build.

### Option C: Checkstyle / PMD / SpotBugs / forbidden-apis

| Dimension | Assessment |
|---|---|
| Complexity | Medium — custom rules are XML or AST visitors |
| Cost | Higher maintenance per rule |
| Scalability | Good for lexical rules, poor for structural ones |
| Team familiarity | High for Checkstyle, low for custom PMD rules |

**Pros:** already present in some builds; source-level, so it sees Lombok annotations directly;
`forbidden-apis` bans an import in one line.
**Cons:** these tools reason about files and tokens, not about architecture. "Adapters may not be
accessed by any layer" and "no cycles between contexts" are natural in ArchUnit and awkward-to-
impossible in Checkstyle. Maintaining structural rules as AST visitors is a project of its own.
**We still use a source scan** for the Lombok checks that bytecode cannot see — but as four lines
inside the ArchUnit test class, not as a second toolchain.

### Option D: Ban Lombok entirely, in every layer

| Dimension | Assessment |
|---|---|
| Complexity | Low to express, high to land |
| Cost | Large one-off migration across every service |
| Scalability | Excellent — one rule, no exceptions, nothing to interpret |
| Team familiarity | High |

**Pros:** removes a compiler plugin that depends on internal JDK APIs; one rule with no allowlist to
argue about; every JDK upgrade gets easier.
**Cons:** the cost lands where the benefit is smallest. `@RequiredArgsConstructor` and `@Slf4j` in
an adapter hide no business behaviour, and removing them means touching thousands of files for no
change in design quality. A ban we cannot finish becomes a rule people learn to ignore, which is
worse than a narrower rule we actually enforce.

---

## Trade-off analysis

**Why block Lombok in the domain but not everywhere.** The cost of Lombok is that behaviour hides
behind generated members and the build depends on a compiler plugin. Both costs are concentrated in
the domain layer: it is the code with the longest life, the most tests, and the most to lose from a
setter that skips an invariant. In an adapter, a constructor and a logger are not design decisions —
they are noise, and generating them hides nothing. Drawing the line at the layer boundary buys most
of the benefit for a fraction of the migration cost.

**Why the domain rule needs four checks, not one.** Lombok's annotations are `RetentionPolicy.SOURCE`
— the compiler erases them before the `.class` file is written, and ArchUnit reads bytecode. The
bytecode rules work only because Lombok stamps `@lombok.Generated` (CLASS retention) on every member
it generates. Measured by the `arch-guardrails` plugin on JDK 25 against Lombok 1.18.38 / 1.18.40 /
1.18.42 / 1.18.46:

| Situation | Bytecode rule | Source scan |
|---|---|---|
| Default, no `lombok.config` | catches it | catches it |
| `addLombokGeneratedAnnotation = true` | catches it | catches it |
| `addLombokGeneratedAnnotation = false` | **blind, passes silently** | catches it |
| `@SneakyThrows` with the marker off | **blind — no trace at all in bytecode** | catches it |

So: two bytecode rules (precise messages), one source scan (cannot be switched off), and one rule
that fails the build if any `lombok.config` sets the marker to `false`. A single line in a config
file three directories up would otherwise turn our main guardrail permanently green.

**Why heuristics are acceptable for the anemic check.** "Every method is an accessor" is not a proof
of an anemic model — an aggregate can legitimately be a data carrier. We accept the false positives
because the rule fires exactly when someone adds a class the team should look at, and the escape
hatch is one line in an allowlist with a comment. A rule that fires occasionally on purpose is worth
more than no rule.

**What we are giving up.** Fitness functions encode structure, never intent. They will not tell us
that an aggregate boundary is wrong, that a use case is doing two things, or that a name lies.
Design review still matters; the tests only stop us from spending it on `@Data`.

---

## The rules we run

Package matchers assume the layout from the `hexagonal-spring` skill: `..domain..`,
`..application..`, `..adapter..`. Adjust the constants, never the rule text.

### Group 1 — Lombok out of the domain *(mandatory, already shipped)*

Installed by the `arch-guardrails` plugin as `architecture/DomainPurityTest.java`. Four checks:
`domain_contains_no_lombok_generated_members`, `domain_has_no_bytecode_dependency_on_lombok`,
`domain_sources_contain_no_lombok_imports`, `lombok_config_does_not_disable_the_bytecode_rule`.

Verify it bites once per repository: add `@Getter` to any domain class, confirm **three** tests
fail, revert.

### Group 2 — the domain model is rich

```java
@ArchTest
static final ArchRule domain_has_no_setters =
        methods().that().areDeclaredInClassesThat().resideInAPackage("..domain..")
                .should().haveNameNotStartingWith("set")
                .because("a setter lets a caller move an object into a state its rules forbid")
                .allowEmptyShould(true);

@ArchTest
static final ArchRule domain_classes_have_behaviour =
        classes().that().resideInAPackage("..domain..").and().areNotInterfaces()
                .should(haveAtLeastOneBusinessMethod())
                .allowEmptyShould(true);

@ArchTest
static final ArchRule domain_fields_are_final =
        fields().that().areDeclaredInClassesThat().resideInAPackage("..domain..")
                .should().beFinal()
                .allowEmptyShould(true);
```

`haveAtLeastOneBusinessMethod()` is a custom condition: a class violates it when every declared
method is `get*`, `set*`, `is*`, or one of `equals` / `hashCode` / `toString` / `canEqual`. That is
the anemic model, stated in a way a build can check.

```java
private static final Set<String> OBJECT_METHODS = Set.of("equals", "hashCode", "toString");

private static ArchCondition<JavaClass> haveAtLeastOneBusinessMethod() {
    return new ArchCondition<>("have at least one method that is not an accessor") {
        @Override
        public void check(JavaClass clazz, ConditionEvents events) {
            boolean hasBehaviour = clazz.getMethods().stream().anyMatch(method -> {
                String name = method.getName();
                if (OBJECT_METHODS.contains(name) || name.equals("canEqual")) {
                    return false;
                }
                return !name.startsWith("get") && !name.startsWith("set") && !name.startsWith("is");
            });
            if (!hasBehaviour) {
                events.add(SimpleConditionEvent.violated(
                        clazz, clazz.getName() + " has no behaviour: every method is an accessor"));
            }
        }
    };
}
```

Add `domain_depends_on_no_framework` from the plugin's `rule-catalog.md` alongside these: no
`org.springframework..`, `jakarta.persistence..`, `jakarta.validation..`, `com.fasterxml.jackson..`
or `org.mapstruct..` inside `..domain..`.

**Expected false positives:** a value object that is genuinely only a typed carrier (`record
CustomerId(UUID value)`) passes, because a record's accessor is `value()`, not `getValue()`. A
domain event that carries data and nothing else may legitimately fail — exempt it by name, with a
comment, rather than weakening the rule.

### Group 3 — controllers stay thin

```java
@ArchTest
static final ArchRule controllers_do_not_touch_entities =
        noClasses().that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().dependOnClassesThat().areAnnotatedWith("jakarta.persistence.Entity")
                .because("an entity in a controller means the web layer is deciding what persistence looks like")
                .allowEmptyShould(true);

@ArchTest
static final ArchRule controllers_do_not_touch_repositories =
        noClasses().that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework.data..", "..adapter.out.persistence..")
                .because("the controller calls a use case; the use case decides how data is reached")
                .allowEmptyShould(true);

@ArchTest
static final ArchRule controllers_are_not_transactional =
        noClasses().that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                .because("a transaction boundary is an application concern, not an HTTP one")
                .allowEmptyShould(true);
```

ArchUnit cannot measure how much logic sits in a method — there is no cyclomatic complexity check.
What it can do is remove the *materials*: a controller with no entity, no repository and no
transaction has very little left to be clever with. That is the whole intent of this group.

### Group 4 — structure

From `rule-catalog.md`, in this order of value: **no cycles between contexts**
(`slices().matching("<base>.(*)..").should().beFreeOfCycles()`), **entities live in the persistence
adapter**, **dependencies point inwards** (`layeredArchitecture()`), **no field injection**.

### Group 5 — the Lombok allowlist outside the domain

Bytecode cannot tell you *which* annotation generated a member: `@UtilityClass`, `@Data` and
`@Builder` all leave the same `@lombok.Generated` marker. Verified on Lombok 1.18.46 / JDK 25, a
`@UtilityClass` compiles to a `public final` class, all members `static`, plus a generated
`private <Name>()` constructor carrying the marker — a recognisable shape, but not a reliable
identity. So the allowlist is enforced by a **source scan** over `src/main/java` outside
`..domain..`: read the `import lombok.…` lines, and fail on anything not in the allowlist.

```java
private static final Set<String> ALLOWED_OUTSIDE_DOMAIN = Set.of(
        "lombok.RequiredArgsConstructor",
        "lombok.extern.slf4j.Slf4j",
        "lombok.Builder",
        "lombok.experimental.UtilityClass");
```

Two rules apply in **every** layer, tests included:

```java
@ArchTest
static final ArchRule entities_have_hand_written_identity =
        classes().that().areAnnotatedWith("jakarta.persistence.Entity")
                .should(haveHandWrittenObjectMethods())   // no @lombok.Generated equals/hashCode/toString
                .allowEmptyShould(true);
```

and a source scan banning `import lombok.SneakyThrows` anywhere.

---

## Lombok policy, annotation by annotation

Legend: ✅ allowed · ⚠️ allowed but discouraged, expect a review question · ❌ fails the build

| Annotation | Domain | Application & adapters | Tests | Reasoning and trade-off |
|---|:--:|:--:|:--:|---|
| `@Data` | ❌ | ❌ on `@Entity`, ⚠️ elsewhere | ⚠️ | On a JPA entity this is our most expensive habit: `toString` walks lazy associations (surprise queries, or `LazyInitializationException`), `equals`/`hashCode` change meaning the moment the id is assigned and break under Hibernate proxies, and the setters are how invariants get skipped. Elsewhere it is usually a record wearing a disguise. |
| `@Value` | ❌ | ⚠️ | ✅ | Superseded by `record` on Java 17+. We are on 25. |
| `@Getter` / `@Setter` | ❌ | ⚠️ | ✅ | A getter that exists only so a service can read a field and decide something is a missing method on the object. A setter on an aggregate is almost always a bug. |
| `@EqualsAndHashCode` | ❌ | ❌ on `@Entity` | ✅ | For an entity, equality is identity: compare the id, by hand, so the intent is visible. |
| `@RequiredArgsConstructor` | ❌ | ✅ | ✅ | Genuine boilerplate removal for constructor injection. Two trade-offs worth knowing: it makes adding a tenth dependency frictionless, so god services grow quietly (consider a companion rule capping constructor parameters); and because the constructor follows field order, reordering fields silently changes a public signature — relevant for shared library modules. |
| `@Slf4j` | ❌ | ✅ | ✅ | Consistent logger declaration, no copy-paste of the wrong class literal. The domain should rarely log at all: it returns results and raises events, and the adapter decides what is worth recording. |
| `@Builder` | ❌ | ✅ on DTOs | ✅ test data | It bypasses the constructor, which is exactly where a domain object checks its invariants — and exactly what a DTO or a test fixture does not have. Same tool, opposite verdict, decided by the layer. |
| `@UtilityClass` | ⚠️ | ✅ | ✅ | **Verified behaviour:** the class becomes `final`, every member becomes `static`, and a `private` constructor is generated. Allowed because the generated code hides no behaviour. Trade-offs: it lives in `lombok.experimental`, the package Lombok explicitly reserves the right to change; the implicit `static` surprises readers and breaks confusingly if someone later adds an instance field; and the hand-written equivalent is three lines. Prefer it for genuine stateless helpers, not as a home for logic that belongs on a domain object. |
| `@With` | ❌ | ⚠️ | ✅ | Records have no wither, so this is tempting. A hand-written `withX` on a value object states intent better and is one line. |
| `@NonNull` | ❌ | ⚠️ | ✅ | `Objects.requireNonNull(x, "x")` in the constructor puts the check where the invariant belongs. |
| `@Cleanup` | ❌ | ❌ | ⚠️ | try-with-resources has been in the language since Java 7. |
| `@SneakyThrows` | ❌ | ❌ | ⚠️ | Banned in `src/main` everywhere. It lies to callers about checked exceptions, and it is the one Lombok feature that leaves **no** trace in bytecode — only the source scan can see it. |

### Costs that apply to Lombok as a whole

These are why the tool stays out of the domain even where the annotation looks harmless:

- **It is a compiler plugin, not a library.** It reaches into internal `javac` APIs. On JDK 25 with
  Lombok 1.18.46, every compilation we ran printed
  `WARNING: sun.misc.Unsafe::objectFieldOffset has been called by lombok.permit.Permit` and
  `sun.misc.Unsafe::objectFieldOffset will be removed in a future release`. Lombok 1.18.36 and
  earlier do not compile on JDK 25 at all. Each JDK upgrade is therefore gated on a Lombok release.
- **JDK 23+ no longer runs annotation processors found on the classpath.** A Lombok dependency
  alone is not enough any more: the build must declare `annotationProcessorPaths` explicitly. When
  it doesn't, Lombok silently generates nothing and the build fails with `cannot find symbol:
  method getName()` — an error that points nowhere near the cause. Verified this week on a fresh
  Spring Boot 4.1.1 / Java 25 project.
- **Tooling friction:** javadoc needs delombok, debuggers step oddly through generated members, and
  coverage needs to be told to skip them (JaCoCo 0.8.2+ ignores `@lombok.Generated`, which is a
  genuine benefit of the marker we also depend on for the guardrail).
- **The language caught up.** Records, compact constructors, `var` and sealed types cover most of
  what Lombok was adopted for in 2015.

---

## How enforcement works: three layers, one rule

| Layer | Mechanism | Strength |
|---|---|---|
| **Write time** | `arch-guardrails` `PreToolUse` hook (`guard-domain-purity.sh`) catches a Lombok import heading into a domain package before the file is saved. Advisory by default; `ARCH_GUARDRAILS_ENFORCE=deny` refuses the write, `=ask` routes it to the developer | Fastest feedback, applies only to agent-written code |
| **Build time** | `DomainPurityTest` and the companion rule groups, run by `./mvnw test` and by CI | **The guarantee.** Same verdict for every author, human or agent |
| **Authoring** | The `archunit-guardrails` skill installs or repairs the test whenever an agent creates a module or adds domain classes | Keeps coverage from rotting as the codebase grows |

The distinction to keep in mind: a skill, a `CLAUDE.md` rule or a review comment is **advice** — it
influences behaviour. An ArchUnit test is **enforcement** — it decides. Anything that must hold
every time belongs in the middle row of that table.

---

## Consequences

**Easier**
- Reviews stop spending their budget on `@Data` and start spending it on aggregate boundaries.
- Agent-written code gets an objective verdict in seconds, so agents can be given more rope safely.
- A new module inherits the rules from the plugin instead of re-deciding them.
- The rules double as documentation: a new joiner reads four test names and knows our standard.

**Harder**
- Legacy modules go red on the first run. That is the rule working, and it needs a migration plan
  (`FreezingArchRule`, an owner, a date) rather than an exemption.
- The anemic heuristic needs per-module tuning; the allowlist needs an owner.
- Build time grows by a few seconds per module (ArchUnit imports and caches all classes on first use).
- One more thing to keep current across JDK and framework upgrades.

**To revisit**
- When a module adopts jMolecules or Spring Modulith, some of these rules are expressed better by
  those libraries' own verification; re-evaluate Group 4 rather than running both.
- When Lombok breaks on a JDK we need, or when the remaining allowlist is small enough that
  Option D becomes cheap, revisit the allowlist.
- If the anemic rule produces more exemptions than findings in a quarter, it is the wrong rule and
  should be replaced, not widened.

---

## Action items

1. [ ] Install the `arch-guardrails` plugin org-wide — it is currently on disk at
       `~/claude-plugins/plugins/arch-guardrails` but **not installed**, so neither the skill nor the
       hooks are active in any session today.
2. [ ] Ratify this ADR with the core team; record dissent in the Trade-off section rather than
       dropping it.
3. [ ] Install `DomainPurityTest` in every module with a `src/main/java`, and verify it bites in
       each repository (add `@Getter` to a domain class → three failures → revert).
4. [ ] Pin `lombok.addLombokGeneratedAnnotation = true` and `config.stopBubbling = true` in each
       repository root `lombok.config`.
5. [ ] Add Group 2, Group 3 and Group 5 to the plugin's template, **in separate commits** from the
       mandatory rule, so a revert of one does not take the others with it.
6. [ ] Freeze existing violations per module with `FreezingArchRule`, each with a named owner and a
       target date; commit `archunit_store/` and review it in PRs like any other file.
7. [ ] Add the architecture tests to the CI gate on `main` for every service.
8. [ ] Add "read ADR-0001" to engineering onboarding, and link it from each repository's
       `CLAUDE.md` / `AGENTS.md` so coding agents surface the same standard.

---

## References

- `plugins/arch-guardrails/skills/archunit-guardrails/SKILL.md` — installation procedure and the
  verified Lombok detection matrix
- `plugins/arch-guardrails/skills/archunit-guardrails/references/rule-catalog.md` — Group 4 rules
- `plugins/arch-guardrails/skills/archunit-guardrails/references/lombok-migration.md` — the
  annotation-by-annotation replacement table and the `FreezingArchRule` recipe
- `plugins/arch-guardrails/skills/archunit-guardrails/references/build-setup.md` — Maven/Gradle
  setup and the "verify it bites" procedure
- The `rich-domain-java` and `hexagonal-spring` skills define the model and the package structure
  these rules assert against
