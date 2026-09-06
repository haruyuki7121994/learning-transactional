# Phase 7 — JPA-specific behavior

Chạy: `./mvnw -Dtest=Phase07JpaTest test`

Mỗi mục: đọc → dự đoán → chạy → thay một yếu tố → giải thích.

## 42. Persistence Context

PC theo dõi managed entities và identity. Cùng entity id trong một PC thường là cùng Java object. Nó không phải transaction, nhưng thường có lifecycle gắn transaction trong app này.

**Thực hành / kết quả:** persistenceContextIdentityAndDirtyChecking assertSame cho hai lần find. Đừng dùng JPA find lặp để suy ra DB isolation.

## 43. Dirty Checking

Hibernate phát hiện thay đổi managed state và đồng bộ xuống DB. Không cần gọi save lại account đã load trong transaction. Detached entity thay đổi không tự lưu.

**Thực hành / kết quả:** Test debit 50 không gọi save, sau commit balance=950. Thử em.detach(a) trước mutation để thấy khác biệt.

## 44. Flush vs Commit

Flush đẩy SQL đang chờ xuống DB, không đảm bảo transaction đã commit. Flush có thể xảy ra trước query hoặc do explicit call; constraint/lock errors có thể xuất hiện lúc này.

**Thực hành / kết quả:** flushIsNotCommit gọi flush rồi rollbackOnly: balance vẫn 1000. Đặt breakpoint SQL để thấy UPDATE đã chạy nhưng bị rollback.

## 45. Lazy Loading

Payment.sender LAZY cần context/session phù hợp khi truy cập field chưa load. open-in-view=false giúp lỗi boundary hiện rõ. Mapping API bằng DTO trong service thường rõ hơn trả graph lazy ra ngoài.

**Thực hành / kết quả:** lazyAssociationNeedsOpenPersistenceContext trả Payment detached rồi getBalance, kỳ vọng LazyInitializationException. getId có thể không initialize proxy nên không dùng làm probe.

## 46. readOnly transaction

Đây là hint cho manager/provider/driver để tối ưu, không phải cơ chế authorization cấm ghi phổ quát. Hibernate có thể đổi flush behavior; chi tiết còn tùy engine/version.

**Thực hành / kết quả:** readOnlyIsAHintVisibleToSpring chứng minh flag. Bài tập: thêm mutation trong readOnly, flush/không flush và JDBC write, ghi lại kết quả từng DB thay vì assume.

## 47. Transaction timeout

Timeout được manager/driver áp dụng tại các điểm resource access thích hợp; nó không phải watchdog ngắt mọi Java code hoặc HTTP call đúng giây thứ N. Inner join không tự thay deadline outer.

**Thực hành / kết quả:** timeoutIsDetectedOnNextJdbcStatement đặt 1 giây, chờ 1.2 giây rồi query/write và nhận TransactionTimedOutException. Thread.sleep chỉ dùng để tạo deadline trong test này.

## Checkpoint

Không nhìn code, giải thích được kết quả từng test và chỉ ra ai tạo transaction, ai commit, database giữ lại gì. Ghi một dự đoán sai và nguyên nhân vào WORKBOOK.md.
