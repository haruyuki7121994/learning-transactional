# Spring Transaction Lab — học qua 8 phase / 55 chủ đề

Project được xây tiếp từ `haruyuki7121994/learning-transactional`, commit gốc `473914a`.
Java 17+, Spring Boot 4.1.1, Maven Wrapper, Spring JDBC + JPA/Hibernate.
Tài liệu tiếng Việt; tên class/method tiếng Anh để dễ đối chiếu Spring source.

## Bắt đầu trong 5 phút

```sh
./mvnw test
./mvnw spring-boot:run
```

Windows: thay `./mvnw` bằng `mvnw.cmd`. Lần đầu cần Internet để tải Maven/dependencies.
Không cần cài Maven hay database cho chế độ H2 mặc định. Mở thư mục này như Maven project trong IntelliJ.
Ứng dụng chỉ nghe tại `127.0.0.1:8080`. H2 in-memory được tạo lại khi restart.

```sh
curl http://localhost:8080/api/accounts
curl -i -X POST http://localhost:8080/api/payments   -H 'Content-Type: application/json'   -d '{"id":"pay-1","from":1,"to":2,"amount":100,"fail":false}'
curl http://localhost:8080/api/accounts
```

Ban đầu hai tài khoản có 1000 đơn vị nhỏ nhất (số nguyên, không dùng float).
Sau thanh toán: 900 và 1100. Đổi id thành `pay-2`, `fail` thành `true`: HTTP 409 và số dư giữ nguyên.
Các lỗi mô phỏng là có chủ đích. File [requests.http](requests.http) dùng được trong IntelliJ HTTP Client.

## Cách học

1. Đọc phase theo thứ tự; mỗi lần chỉ học một nhóm nhỏ.
2. **Dự đoán** số row/số dư và exception trước khi chạy.
3. Chạy test, đọc SQL/transaction log, đặt breakpoint.
4. Sửa một annotation hoặc cách gọi theo bài tập, chạy lại và giải thích chênh lệch.
5. Ghi câu trả lời vào [WORKBOOK.md](docs/WORKBOOK.md), rồi mới xem lời giải trong test.

Ví dụ chạy một phase / một thí nghiệm:

```sh
./mvnw -Dtest=Phase03RollbackTest test
./mvnw '-Dtest=Phase03RollbackTest#checkedCommitsByDefault' test
```

Test **không** có `@Transactional` trên class: chúng kiểm tra trạng thái sau commit/rollback thật,
tránh transaction của test che mất self-invocation hoặc after-commit.
Không chạy các test song song vì cùng dùng fixture database.

| Phase | Hướng dẫn | Test chính | Kiểu thực hành |
|---|---|---|---|
| 1 · Fundamentals | [01](docs/phase-01.md) | Phase01FundamentalsTest | JDBC và programmatic transaction |
| 2 · Machinery | [02](docs/phase-02.md) | Phase02ProxyTest | Proxy + debugger |
| 3 · Semantics | [03](docs/phase-03.md) | Phase03RollbackTest | 8 tình huống commit/rollback |
| 4 · Design | [04](docs/phase-04.md) | Phase04DesignTest | Payment + event + outbox |
| 5 · Propagation | [05](docs/phase-05.md) | Phase05PropagationTest | 7 propagation modes |
| 6 · Concurrency | [06](docs/phase-06.md) | Phase06ConcurrencyTest | 2 connections/threads + MySQL SQL lab |
| 7 · JPA | [07](docs/phase-07.md) | Phase07JpaTest | PC, flush, lazy, timeout |
| 8 · Cross-cutting | [08](docs/phase-08.md) | Phase08CrossCuttingTest | Advice/retry/async + distributed design exercise |

[Danh sách đầy đủ 55 chủ đề](docs/CURRICULUM.md).
Distributed transaction và Saga là bài tập thiết kế/capstone, chưa triển khai XA coordinator hay microservice cluster.
Outbox relay có sender/receiver mô phỏng để tái hiện crash window; dữ liệu outbox thật nằm trong DB.

## Cấu trúc để đọc code

```text
src/main/java/com/learning/learningtransactional/
  LabConfig              # named JDBC manager + primary JPA manager
  SemanticsLab           # rollback và self-invocation
  PropagationLab         # inner bean qua proxy
  BoundaryLab            # outer transaction gọi inner bean
  PaymentService         # service boundary; debit/credit + payment + outbox
  Account / Payment      # @Version, managed entity, lazy association
  PaymentEvents          # AFTER_COMMIT listener
  OutboxRelay             # send ngoài transaction, acknowledge trong transaction
  AsyncLab               # worker thread và transaction mới
src/test/java/.../Phase*Test.java
```

Hai transaction manager phục vụ các lab riêng biệt trên **cùng một DataSource**.
Không lồng JPA manager vào JDBC manager hoặc ngược lại; đây không phải ví dụ atomic multi-database.
NESTED chỉ dùng JDBC/savepoint; không giả định nó rollback được state trong JPA persistence context.

## MySQL thật cho phase 6

```sh
docker compose up -d --wait
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

Compose tạo service lab tại port **3307**, database `txlab`, user/password `txlab`.
Không dùng container `local_mysql`/port 3306 của repo gốc. MySQL data được giữ trong volume riêng.
Profile MySQL dùng `ddl-auto=update`; chỉ dùng cho database học tập này.
Dừng service: `docker compose stop`. Không cần xóa volume.

Có thể chạy suite trên **database lab chuyên dụng** (fixture sẽ xóa bảng lab/account/payment/outbox):

```sh
./mvnw test -Dspring.profiles.active=mysql
```

Không chạy ứng dụng và suite đồng thời trên cùng MySQL lab. Hướng dẫn 2 session và SQL: [MYSQL-LAB.md](docs/MYSQL-LAB.md).
H2 không phải bản sao MySQL: không suy luận behavior DB thật chỉ từ H2 test.

## Đọc log và debug

Tìm `Getting transaction`, `Creating new transaction`, `Participating in existing transaction`,
`Suspending`, `savepoint`, `Initiating transaction commit/rollback`.
Bật SQL khi cần: `--spring.jpa.show-sql=true` trong program arguments của IDE.
Breakpoint: `TransactionInterceptor.invoke`, `TransactionAspectSupport.invokeWithinTransaction`,
`AbstractPlatformTransactionManager.getTransaction/processCommit/processRollback`, `PaymentService.pay`.

## Nguồn đối chiếu

- [Spring annotation/proxy semantics](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)
- [Spring propagation](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html)
- [Spring transaction events](https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html)
- [Spring Data JPA transactionality](https://docs.spring.io/spring-data/jpa/reference/jpa/transactions.html)
- [MySQL InnoDB isolation](https://dev.mysql.com/doc/refman/8.0/en/innodb-transaction-isolation-levels.html)
- [MySQL deadlocks](https://dev.mysql.com/doc/refman/8.0/en/innodb-deadlocks-handling.html)

Xem [VALIDATION.md](docs/VALIDATION.md) để biết chính xác các kiểm tra đã chạy khi tạo project.
