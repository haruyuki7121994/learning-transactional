# Curriculum — 55 chủ đề

Giữ đúng thứ tự yêu cầu. Test có lời giải chạy được; bài MySQL chạy thủ công theo hai session; các phần Saga/XA là bài tập thiết kế được ghi rõ.

| # | Chủ đề | Tài liệu |
|---|---|---|
| 1 | Database transaction là gì? | [Phase 1](phase-01.md) |
| 2 | ACID | [Phase 1](phase-01.md) |
| 3 | BEGIN / COMMIT / ROLLBACK | [Phase 1](phase-01.md) |
| 4 | JDBC Connection và transaction | [Phase 1](phase-01.md) |
| 5 | PlatformTransactionManager | [Phase 1](phase-01.md) |
| 6 | TransactionDefinition / TransactionStatus | [Phase 1](phase-01.md) |
| 7 | @Transactional thực chất là gì? | [Phase 2](phase-02.md) |
| 8 | Spring AOP Proxy | [Phase 2](phase-02.md) |
| 9 | TransactionInterceptor | [Phase 2](phase-02.md) |
| 10 | External call vào pay() | [Phase 2](phase-02.md) |
| 11 | JDK Proxy vs class-based proxy | [Phase 2](phase-02.md) |
| 12 | Transaction flow | [Phase 2](phase-02.md) |
| 13 | Self Invocation | [Phase 2](phase-02.md) |
| 14 | Cách giải quyết self-invocation | [Phase 2](phase-02.md) |
| 15 | Rollback Rules | [Phase 3](phase-03.md) |
| 16 | RuntimeException vs checked Exception | [Phase 3](phase-03.md) |
| 17 | catch/swallow exception | [Phase 3](phase-03.md) |
| 18 | rollbackFor / noRollbackFor | [Phase 3](phase-03.md) |
| 19 | rollbackOnly | [Phase 3](phase-03.md) |
| 20 | UnexpectedRollbackException | [Phase 3](phase-03.md) |
| 21 | Transaction boundary | [Phase 4](phase-04.md) |
| 22 | Service layer boundary | [Phase 4](phase-04.md) |
| 23 | Long-running transaction | [Phase 4](phase-04.md) |
| 24 | External HTTP call trong transaction | [Phase 4](phase-04.md) |
| 25 | Transaction + event | [Phase 4](phase-04.md) |
| 26 | Outbox pattern | [Phase 4](phase-04.md) |
| 27 | Propagation.REQUIRED | [Phase 5](phase-05.md) |
| 28 | REQUIRES_NEW | [Phase 5](phase-05.md) |
| 29 | NESTED | [Phase 5](phase-05.md) |
| 30 | SUPPORTS | [Phase 5](phase-05.md) |
| 31 | MANDATORY | [Phase 5](phase-05.md) |
| 32 | NOT_SUPPORTED | [Phase 5](phase-05.md) |
| 33 | NEVER | [Phase 5](phase-05.md) |
| 34 | Isolation levels | [Phase 6](phase-06.md) |
| 35 | Dirty Read | [Phase 6](phase-06.md) |
| 36 | Non-repeatable Read | [Phase 6](phase-06.md) |
| 37 | Phantom Read | [Phase 6](phase-06.md) |
| 38 | Lost Update | [Phase 6](phase-06.md) |
| 39 | Optimistic Lock | [Phase 6](phase-06.md) |
| 40 | Pessimistic Lock | [Phase 6](phase-06.md) |
| 41 | Deadlock | [Phase 6](phase-06.md) |
| 42 | Persistence Context | [Phase 7](phase-07.md) |
| 43 | Dirty Checking | [Phase 7](phase-07.md) |
| 44 | Flush vs Commit | [Phase 7](phase-07.md) |
| 45 | Lazy Loading | [Phase 7](phase-07.md) |
| 46 | readOnly transaction | [Phase 7](phase-07.md) |
| 47 | Transaction timeout | [Phase 7](phase-07.md) |
| 48 | AOP Ordering | [Phase 8](phase-08.md) |
| 49 | Transaction + Retry | [Phase 8](phase-08.md) |
| 50 | Transaction + @Async | [Phase 8](phase-08.md) |
| 51 | ThreadLocal | [Phase 8](phase-08.md) |
| 52 | Multiple TransactionManager | [Phase 8](phase-08.md) |
| 53 | TransactionalEventListener | [Phase 8](phase-08.md) |
| 54 | Distributed transaction | [Phase 8](phase-08.md) |
| 55 | Saga / Outbox / Idempotency | [Phase 8](phase-08.md) |
