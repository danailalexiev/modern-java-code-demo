---
name: java-sealed-outcomes
description: Use when writing or changing Java service, controller or domain code that can fail for a business reason (not found, invalid state, rejected, conflict). Models outcomes as sealed types and handles them with exhaustive pattern matching.
---
# Sealed outcomes

## Rules
1. Expected business outcomes are values. A method that can fail for a business reason returns `Result<E, T>`, where `E` is a sealed interface of records declared next to the use case.
2. Exceptions are only for infrastructure failures and programmer errors.
3. Never use an open error type: no `Result<String, T>`, `Result<Exception, T>` or `Result<Object, T>`.
4. Every switch over a sealed type has no `default` and no catch-all pattern.
5. After adding a permitted subtype, run `./gradlew -q compileJava`. Fix every non-exhaustive switch it reports by handling the new case explicitly.
6. A `@Transactional` method returning `Result` does all checks before its first write, or uses `TransactionTemplate` with `setRollbackOnly()` on `Err`.
7. Controllers map every case to a `ProblemDetail` in one switch expression.

## Reference code
- `Result.java`, `UpgradeError.java`, `AccountService.upgrade`, `AccountController.upgrade`

## Done when
- `./gradlew check` passes, including the `NoDefaultOnSealedSwitch` check and `OutcomeArchitectureTest`.
