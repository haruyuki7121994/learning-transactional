# Phase 4 — Transaction design

Chạy: `./mvnw -Dtest=Phase04DesignTest test`

Mỗi mục: đọc → dự đoán → chạy → thay một yếu tố → giải thích.

## 21. Transaction boundary

Boundary nên trùng invariant cần bảo vệ. Payment này bao gồm hai wallet, payment record và outbox row. Không chỉ bọc từng repository.save riêng lẻ.

**Thực hành / kết quả:** serviceBoundaryCommitsBothWalletsAndOutbox và failureBetweenDebitAndCreditRollsBackEverything chứng minh cùng commit/rollback.

## 22. Service layer boundary

Service điều phối use case; repository xử lý persistence. Transaction ở service cho phép load + mutate nhiều entity cùng context. Validation cú pháp đơn giản có thể thực hiện trước khi giữ lock.

**Thực hành / kết quả:** Đọc pay: khóa theo id tăng dần, debit, credit, ghi payment/outbox. Thử bỏ @Transactional và quan sát lỗi lock/atomicity; khôi phục sau bài tập.

## 23. Long-running transaction

Giữ transaction lâu có thể giữ connection, row lock và snapshot lâu. Thời gian transaction không đồng nghĩa CPU đang làm việc; chờ network vẫn giữ resource.

**Thực hành / kết quả:** Bài tập thiết kế: thêm mô phỏng chờ sau khi khóa account, gửi hai payment chung account, quan sát request thứ hai chờ. Sau đó đưa phần chờ ra ngoài boundary.

## 24. External HTTP call trong transaction

Local DB rollback không thể thu hồi request đã được dịch vụ khác xử lý. Timeout còn mơ hồ: server có thể đã thành công. Cần idempotency key, trạng thái pending và reconciliation/compensation.

**Thực hành / kết quả:** OutboxRelay mô phỏng send ở ngoài TransactionTemplate. Vẽ 3 trường hợp: fail trước send, sau send, sau acknowledge. Không gọi dịch vụ thanh toán thật trong lab.

## 25. Transaction + event

Event notification và dữ liệu bền vững là hai việc khác nhau. Listener sau commit hữu ích cho công việc chỉ được bắt đầu khi DB thành công, nhưng process có thể chết trước khi listener xử lý.

**Thực hành / kết quả:** Kiểm tra events.committed ở test success và rollback. Một list trong RAM không phải message queue bền vững.

## 26. Outbox pattern

Ghi business state và outbox trong một local transaction. Worker đọc pending, gửi, rồi đánh dấu delivered. Crash giữa gửi và đánh dấu tạo duplicate nên consumer cần dedup bền vững.

**Thực hành / kết quả:** outboxSurvivesCrashWindowAndReceiverDeduplicates mô phỏng chính crash window. Receiver dùng Set in-memory chỉ để học; production cần inbox UNIQUE(event_id) cùng transaction với side effect, batch/claim/lease, backoff và observability.

## Checkpoint

Không nhìn code, giải thích được kết quả từng test và chỉ ra ai tạo transaction, ai commit, database giữ lại gì. Ghi một dự đoán sai và nguyên nhân vào WORKBOOK.md.
