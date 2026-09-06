# Phase 1 — Transaction fundamentals

Chạy: `./mvnw -Dtest=Phase01FundamentalsTest test`

Mỗi mục: đọc → dự đoán → chạy → thay một yếu tố → giải thích.

## 1. Database transaction là gì?

Một nhóm thao tác database có chung kết quả thành công hoặc bị hủy. Chuyển 100 từ A sang B cần debit và credit cùng tồn tại. Một câu SQL tự động commit cũng là một transaction nhỏ.

**Thực hành / kết quả:** Chạy jdbcCommitAndRollback. Row jdbc-commit tồn tại, jdbc-rollback không tồn tại. Bỏ c.commit() rồi dự đoán kết quả trước khi chạy.

## 2. ACID

Atomicity: không debit một nửa. Consistency: giữ invariant như tổng tiền A+B=2000; application và constraint phải định nghĩa invariant. Isolation: quy định transaction khác được quan sát gì. Durability: commit được bảo vệ theo cơ chế log/storage của DB, không có nghĩa dữ liệu in-memory sống qua restart.

**Thực hành / kết quả:** Chạy Phase04DesignTest. Viết invariant tổng tiền; chuyển fail=true. H2 không phải thí nghiệm durability sau crash. Dùng MySQL volume để quan sát dữ liệu sau restart thông thường.

## 3. BEGIN / COMMIT / ROLLBACK

MySQL dùng START TRANSACTION hoặc BEGIN. COMMIT hoàn tất; ROLLBACK hủy phần chưa commit. ROLLBACK không hoàn tác commit đã xong hay email đã gửi. DDL có thể có implicit commit tùy database.

**Thực hành / kết quả:** So sánh SQL với c.setAutoCommit(false), c.commit(), c.rollback() trong test. Thêm insert thứ hai trước rollback: cả hai insert chưa commit đều biến mất.

## 4. JDBC Connection và transaction

Local JDBC transaction thuộc connection. Statement trên connection khác không tự tham gia. Connection pool tái sử dụng connection sau khi trả lại; code phải đóng resource và kết thúc transaction. Spring JDBC lấy connection đã gắn với thread khi có transaction.

**Thực hành / kết quả:** Debug connection của EntryStore khi chạy trong TransactionTemplate và ngoài nó. Ngoài transaction, insert của lab dùng auto-commit.

## 5. PlatformTransactionManager

Đây là API quản lý lifecycle, không phải database. LabConfig chọn JpaTransactionManager cho EntityManager và DataSourceTransactionManager cho JDBC. Spring Boot vẫn có thể tự cấu hình manager nếu không khai báo riêng.

**Thực hành / kết quả:** Chạy managerDefinitionAndStatus; thay rollback bằng commit và đổi expected row. Đặt breakpoint ở getTransaction.

## 6. TransactionDefinition / TransactionStatus

Definition mô tả propagation, isolation, timeout, readOnly, name. Status là handle của lần tham gia/tạo transaction: new transaction hay không, rollback-only, completed, savepoint nếu hỗ trợ. Không phải mỗi status đều đại diện connection mới.

**Thực hành / kết quả:** Chạy templateCanMarkRollbackOnly. Đặt name cho definition, tìm trong log. TransactionTemplate gói acquire/commit/rollback giúp tránh quên cleanup.

## Checkpoint

Không nhìn code, giải thích được kết quả từng test và chỉ ra ai tạo transaction, ai commit, database giữ lại gì. Ghi một dự đoán sai và nguyên nhân vào WORKBOOK.md.
