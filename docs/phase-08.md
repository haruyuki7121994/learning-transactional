# Phase 8 — Cross-cutting issues

Chạy: `./mvnw -Dtest=Phase08CrossCuttingTest test`

Mỗi mục: đọc → dự đoán → chạy → thay một yếu tố → giải thích.

## 48. AOP Ordering

Advice bên ngoài vào trước, ra sau. @Order nhỏ thường có precedence cao hơn. Retry nằm ngoài transaction advice mới có thể retry cả lỗi commit. ProxyFactory test khai báo thứ tự trực tiếp để quan sát chính xác.

**Thực hành / kết quả:** outerAdviceWrapsTransactionInterceptor có trace before:false, inside:true, after:false. Đảo addAdvice, giải thích vì sao before/after có thể ở trong transaction.

## 49. Transaction + Retry

Retry transient conflict bằng transaction mới cho mỗi attempt. Retry bên trong một transaction đã rollback-only không sửa được transaction đó. Chỉ retry lỗi phù hợp, giới hạn attempts và backoff; tránh lặp side effect ngoài DB.

**Thực hành / kết quả:** retryWrapsFreshTransactionPerAttempt thất bại 2 lần rồi commit 1 row. Đây là retry loop minh họa, không phụ thuộc annotation retry/library.

## 50. Transaction + @Async

Async đổi thread, caller transaction không tự truyền sang worker. Worker có thể tự mở transaction nếu được advice đúng cách; tránh truyền managed entity vào worker.

**Thực hành / kết quả:** asyncDoesNotInheritCallerTransaction: worker thường false, worker có annotation riêng true. Dùng future có deadline để test không treo.

## 51. ThreadLocal

Imperative Spring transaction gắn resource/context với thread. Không copy TransactionSynchronizationManager state sang executor. Reactive transaction dùng context khác; bài này chỉ imperative.

**Thực hành / kết quả:** Debug tên thread và isActualTransactionActive ở AsyncLab và caller. Truyền id rồi reload entity trong worker thay vì share EntityManager.

## 52. Multiple TransactionManager

Chọn manager bằng qualifier hoặc @Transactional(transactionManager=...). @Primary chọn default, không biến hai local managers thành distributed transaction.

**Thực hành / kết quả:** explicitManagersAreDifferentImplementations kiểm tra 2 manager. Bài tập thiết kế: thêm DB thứ hai và ghi ra partial-commit timeline; cần XA hoặc eventual consistency phù hợp, không chỉ thêm annotation.

## 53. TransactionalEventListener

Có thể gắn listener với phase transaction; default AFTER_COMMIT. Không có transaction thì default không gọi listener. Nếu listener cần ghi DB sau commit, dùng boundary transaction mới rõ ràng.

**Thực hành / kết quả:** transactionalListenerDoesNotRunForRolledBackPublish publish thật rồi rollback outer; listener không nhận. Event được giữ trong RAM có thể mất khi process chết.

## 54. Distributed transaction

Một local manager chỉ phối hợp local resource tương ứng. XA/2PC cần các resource hỗ trợ XA và coordinator để prepare/commit/recover; nó có chi phí và failure modes riêng. REST call bất kỳ không tự trở thành XA participant.

**Thực hành / kết quả:** Bài tập thiết kế trong CAPSTONE.md: vẽ DB1 commit, DB2 fail. So sánh coordinator recovery với Saga compensation. Repo không giả lập XA bằng hai @Transactional.

## 55. Saga / Outbox / Idempotency

Saga là chuỗi local commits với compensating actions khi bước sau thất bại. Compensation là nghiệp vụ mới, có thể fail/retry, không xóa lịch sử như rollback. Outbox giải quyết durable publish; idempotency chống effect lặp.

**Thực hành / kết quả:** Làm CAPSTONE.md: reserve → pay → confirm hoặc release/refund. Outbox crash test là phần chạy sẵn; Saga và durable inbox là phần bạn tự mở rộng với acceptance criteria.

## Checkpoint

Không nhìn code, giải thích được kết quả từng test và chỉ ra ai tạo transaction, ai commit, database giữ lại gì. Ghi một dự đoán sai và nguyên nhân vào WORKBOOK.md.
