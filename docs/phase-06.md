# Phase 6 — Database concurrency

Chạy: `./mvnw -Dtest=Phase06ConcurrencyTest test`

Mỗi mục: đọc → dự đoán → chạy → thay một yếu tố → giải thích.

## 34. Isolation levels

READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE là hợp đồng isolation. Implementation MVCC/locking và default phụ thuộc DB. Isolation trên inner join REQUIRED không tự đổi transaction outer.

**Thực hành / kết quả:** Chạy readCommittedAllowsNonRepeatableRead và repeatableReadKeepsSnapshot. Sau đó làm MYSQL-LAB để thấy behavior InnoDB thực tế.

## 35. Dirty Read

Transaction A đọc dữ liệu B chưa commit; nếu B rollback, A đã dùng giá trị chưa từng tồn tại bền vững.

**Thực hành / kết quả:** MYSQL-LAB mục Dirty read dùng 2 session và READ UNCOMMITTED. B thay 1000→900 chưa commit, A thấy 900, B rollback.

## 36. Non-repeatable Read

Cùng một row đọc hai lần trong một transaction cho giá trị khác do transaction khác commit update.

**Thực hành / kết quả:** Test READ_COMMITTED thấy 100 rồi 200. Test REPEATABLE_READ thấy 100 rồi 100. Dùng JDBC để không nhầm với JPA first-level cache.

## 37. Phantom Read

Một predicate query trả về tập row khác sau insert/delete của transaction khác. Cần phân biệt consistent read và locking/current read trong InnoDB.

**Thực hành / kết quả:** MYSQL-LAB có RC count 2→3, RR snapshot count 2→2. RR consistent reads của InnoDB có thể ngăn phantom trong ví dụ này, không dùng bảng lý thuyết để phủ nhận kết quả.

## 38. Lost Update

Hai caller đọc cùng balance cũ rồi ghi giá trị tuyệt đối; lần ghi sau đè kết quả lần trước. Transaction tồn tại không tự bảo vệ mọi read-modify-write.

**Thực hành / kết quả:** staleReadModifyWriteLosesUpdateWithoutVersion kết thúc 80 thay vì 70. So sánh UPDATE balance=balance-? và thêm @Version.

## 39. Optimistic Lock

Account có @Version. Hai persistence context đọc cùng version; update của context stale bị từ chối. Retry phải reload và tính lại trong transaction mới, không reuse entity stale.

**Thực hành / kết quả:** optimisticVersionRejectsStaleEntity commit debit 10, debit 20 stale thất bại; balance=990.

## 40. Pessimistic Lock

SELECT FOR UPDATE khóa row để transaction thứ hai chờ. Có thể gây timeout/deadlock, nên giữ boundary ngắn và khóa theo thứ tự ổn định.

**Thực hành / kết quả:** pessimisticLockSerializesTwoDebits dùng 2 worker và latch, worker thứ hai chờ rồi balance=970.

## 41. Deadlock

A giữ row 1 chờ row 2; B giữ row 2 chờ row 1. DB chọn victim, application xử lý retry có giới hạn. Deadlock khác lock timeout: timeout có thể chỉ là chờ quá lâu mà không có chu trình.

**Thực hành / kết quả:** MYSQL-LAB tái hiện 2 session đối nghịch. PaymentService khóa min(id) trước max(id) để tránh mẫu này; không đảm bảo mọi deadlock trong toàn hệ thống biến mất.

## Checkpoint

Không nhìn code, giải thích được kết quả từng test và chỉ ra ai tạo transaction, ai commit, database giữ lại gì. Ghi một dự đoán sai và nguyên nhân vào WORKBOOK.md.
