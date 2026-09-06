# Validation — 2026-09-06

Môi trường: macOS, Amazon Corretto JDK 21.0.5; compile target Java 17; Maven Wrapper 3.9.11; Spring Boot 4.1.1.

| Kiểm tra | Kết quả |
|---|---|
| `./mvnw package` trên H2 | BUILD SUCCESS; 40 passed, 4 MySQL-only tests skipped |
| `./mvnw test -Dspring.profiles.active=mysql` | BUILD SUCCESS; 44 passed, 0 skipped |
| MySQL thật | Docker image mysql:8.0, service riêng tại port 3307 |
| JDBC / proxy / rollback / propagation | Commit và rollback được assert từ database ngoài test transaction |
| Concurrency | RC/RR visibility, lost update, optimistic conflict, pessimistic blocking |
| MySQL-specific | Dirty read, RC phantom, RR snapshot, deadlock error 1213 |
| JPA / cross-cutting | PC identity, dirty checking, flush rollback, lazy exception, timeout, ordering, retry, async, events |
| HTTP smoke trên JAR | GET initial 1000/1000; POST success 900/1100; simulated failure 409 giữ số dư; duplicate 409 giữ số dư |
| Documentation | Đủ 55 mục theo thứ tự; local links checked |
| Git whitespace | `git diff --check` passed |

MySQL suite chạy trước bước format Java; package H2 chạy lại sau format. Không có thay đổi logic giữa hai lần.
API smoke dùng port tạm 18080; process kiểm tra và MySQL lab đã được dừng sau validation.
MySQL `local_mysql` có sẵn tại port 3306 không được sửa đổi.

## Giới hạn

- H2 là chế độ bắt đầu nhanh; MySQL isolation được kiểm tra riêng trên MySQL thật.
- Source target Java 17; lần kiểm tra này dùng JDK 21, chưa chạy riêng bằng JDK 17.
- Saga/XA là design exercises, chưa triển khai distributed coordinator/microservices.
- Outbox receiver dùng Set in-memory mô phỏng dedup, chưa có durable inbox/broker hay restart recovery end-to-end.
- SQL manual có automated MySQL tests tương ứng; không có external payment/HTTP service thật.
- Project được xây tiếp từ commit gốc `473914a`; các thay đổi của khóa học nằm trên branch `lab`.
