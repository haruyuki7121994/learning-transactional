# Phase 5 — Advanced transaction / propagation

Chạy: `./mvnw -Dtest=Phase05PropagationTest test`

Mỗi mục: đọc → dự đoán → chạy → thay một yếu tố → giải thích.

## 27. Propagation.REQUIRED

Dùng transaction hiện có của manager hoặc tạo mới. Nhiều logical scope có thể dùng chung một physical transaction. Inner thường kế thừa đặc tính outer khi join.

**Thực hành / kết quả:** Chạy test UnexpectedRollback ở phase 3; dự đoán vì sao inner không commit độc lập.

## 28. REQUIRES_NEW

Tạm ngưng outer, mở transaction độc lập. Outer vẫn có thể giữ lock/connection. Inner cần thêm connection; đừng dùng pool size 1 để học tình huống này.

**Thực hành / kết quả:** requiresNewCommitsEvenWhenOuterRollsBack: audit còn, outer mất. Nếu inner đụng row outer đã khóa, nó có thể chờ chính outer.

## 29. NESTED

Một physical transaction, savepoint trước inner. Rollback inner quay về savepoint; outer commit thì phần còn lại tồn tại. Outer rollback vẫn xóa mọi thay đổi chưa commit, kể cả phần inner thành công.

**Thực hành / kết quả:** nestedRollsBackOnlyToSavepoint: outer/after còn, inner mất. Lab dùng jdbcTxManager; không chuyển thẳng annotation sang JPA và giả định PC được khôi phục.

## 30. SUPPORTS

Có transaction thì join, không có thì không ép tạo physical transaction. Synchronization context và actual transaction không hoàn toàn giống nhau.

**Thực hành / kết quả:** supportsWithAndWithoutOuter: false ngoài outer, true trong outer.

## 31. MANDATORY

Yêu cầu caller đã mở transaction của manager tương ứng. Phù hợp operation nội bộ không có ý nghĩa độc lập.

**Thực hành / kết quả:** mandatoryRequiresOuter: ngoài outer ném IllegalTransactionStateException, trong outer chạy được.

## 32. NOT_SUPPORTED

Tạm ngưng transaction hiện tại để chạy ngoài nó. SQL auto-commit ở đoạn này không rollback cùng outer. Suspend không giải phóng lock của outer.

**Thực hành / kết quả:** notSupportedSuspendsOuter: false trong inner, quay lại true sau khi inner kết thúc.

## 33. NEVER

Yêu cầu không có transaction, nếu có thì từ chối. Khác NOT_SUPPORTED ở chỗ không suspend để chiều caller.

**Thực hành / kết quả:** neverRejectsOuter: ngoài outer false, trong outer ném IllegalTransactionStateException.

## Checkpoint

Không nhìn code, giải thích được kết quả từng test và chỉ ra ai tạo transaction, ai commit, database giữ lại gì. Ghi một dự đoán sai và nguyên nhân vào WORKBOOK.md.
