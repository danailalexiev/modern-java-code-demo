# accounts

A small Spring Boot service, built up one git tag at a time, that demonstrates a
progression from exception-based error handling to **sealed, exhaustive,
data-oriented outcomes** in Java 25.

The thesis: expected business outcomes should be values the compiler can check,
not exceptions it ignores. Each tag below is a self-contained step in that
argument — every one of them passes `./gradlew check` on its own, so you can
check out any tag and it just works.

## The service

Two endpoints:

- `GET /users/{id}`
- `POST /users/{id}/upgrade` — the workhorse, because it has several failure
  modes and a write, which is what makes composition and transactions
  interesting.

Business rules for an upgrade: the user must exist, must not be suspended, and
must have a valid payment method. Seed data (`data.sql`), unchanged across
every tag:

| id | state |
| -- | -- |
| 1 | active, has a payment method on file |
| 2 | active, **no** payment method on file |
| 3 | suspended |

## Stack

Java 25, Spring Boot 4.1, Spring Data JPA, H2, Gradle (Kotlin DSL), JUnit
Jupiter. A `:checks` Gradle subproject holds a custom Error Prone check, wired
in from `step-5-agent` onward.

## Tags

Check out any tag and build it in isolation:

```
git checkout <tag>
./gradlew check
```

### `step-0-baseline` — the strong exception-based baseline

Idiomatic Spring: `UserEntity` / `UserRepository`, a `UserService.upgrade` that
throws `UserNotFoundException` / `UserSuspendedException`, and `ApiErrors`
(`@RestControllerAdvice` + `ProblemDetail`) mapping them to RFC 9457 responses.
A stub `BillingClient` exists but isn't wired in yet, and there's no
payment-method check on `upgrade` — that's added live as a demo of what
*isn't* forced by this design: nothing about the method signature says how it
can fail, and the link between a thrown exception and its handler lives in
the author's head, not the compiler.

### `step-1-sealed` — sealed outcomes, exhaustive switches

`UpgradeResult` is a sealed interface (`Upgraded` / `NotFound` / `Suspended` /
`PaymentRequired`); `UserService.upgrade` returns one instead of throwing, and
the controller maps every case in a single exhaustive `switch` expression.
`ApiErrors` is gone. Add a new permitted subtype and the switch stops
compiling — that's the guarantee this buys: a reader (human or agent) finds
every failure mode in the return type, and the compiler enforces that every
one of them is handled.

### `step-2-domain` — illegal states made unrepresentable

`Account` (sealed: `Active` / `Suspended` / `PendingVerification`), `Email`
and `UserId` model the domain itself, not just the outcomes — so a suspended
account always has a `since`, and there's no null to forget.
`JpaAccountRepositoryAdapter` maps the JPA row (which still allows nonsensical
field combinations) onto `Account` at the persistence boundary, since records
can't be JPA entities. The `domain` package is `@NullMarked`. Not yet wired
into the live request path — that's next.

### `step-3-result` — a generic `Result<E, T>` with sealed errors

`Result` (`Ok` / `Err`) composes with `map` / `flatMap`; `UpgradeError` is a
sealed interface of records declared next to the use case. `UserService` /
`UserController` become `AccountService` / `AccountController`, now operating
on `Account` / `UserId` throughout. `upgrade` is a `flatMap` chain: find →
require active → require payment method → save. The controller switch uses
nested record patterns and unnamed patterns (`Err(AccountSuspended(_, var
since))`, `Err(NotVerified _)`).

### `step-4-tx-bug` — the transaction rollback gotcha

`audit.record` runs first inside `@Transactional upgrade()`. `@Transactional`
only rolls back on unchecked exceptions, and returning `Result.Err` looks like
a normal return to Spring — so the audit row for a rejected upgrade still
commits. `AccountServiceTransactionTest` demonstrates it directly against the
audit log.

### `step-4-tx` — the fix

`audit.record` moves inside the final `map` step, after every check has
passed, so a rejected upgrade writes nothing. Also adds
`upgradeWithRollbackOnError`, an alternative for when checks can't be ordered
before every write: it writes first but uses `TransactionTemplate` +
`setRollbackOnly()` to undo the whole transaction — audit write included — on
`Err`.

### `step-5-agent` — a deterministic guardrail

The rule an agent breaks first under pressure (never put a `default` on a
switch over a sealed type) now fails the build two ways:

- **`NoDefaultOnSealedSwitch`** — an Error Prone `BugChecker` (in `:checks`)
  that flags `default` only when the switch's selector type is actually
  sealed.
- **Checkstyle** — a cruder, deterministic backstop banning the literal text
  `default ->` in the `api`, `service` and `domain` packages, no compiler
  internals required.

`OutcomeArchitectureTest` (ArchUnit) guards the architecture itself: no
business exceptions in `service`, every `*Error` sealed interface is actually
sealed, no `@ExceptionHandler` in `api`. `.claude/skills/java-sealed-outcomes/`
tells an agent the rules in plain language; the build enforces them
regardless of whether the agent follows them.

## Scripts

- **`scripts/verify-tags.sh`** — checks out every tag above, runs
  `./gradlew check`, then runs three negative checks that are supposed to
  *fail* the build (a non-exhaustive switch after adding a new sealed
  subtype, a `default ->` tripping the guardrail, a business exception
  smuggled into `service`) via patches under `scripts/negative/`, and prints
  a PASS/FAIL table.
- **`scripts/reset.sh <tag>`** — checks out a tag and warms the compiler.
- **`requests.http`** — the requests for exercising each tag, with curl
  equivalents in comments.
