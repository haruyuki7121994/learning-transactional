# Phase 3 — Transaction semantics

Chạy: `./mvnw -Dtest=Phase03RollbackTest test`

Mỗi mục: đọc → dự đoán → chạy → thay một yếu tố → giải thích.

## 15. Rollback Rules

Proxy quyết định dựa vào exception thoát khỏi method và transaction status. Normal return thường commit; commit vẫn có thể thất bại do constraint, timeout hoặc rollback-only.

**Thực hành / kết quả:** Chạy normalReturnCommits và runtimeRollsBack. Tìm dòng commit/rollback trong log.

## 16. RuntimeException vs checked Exception

Project giữ default của Spring: RuntimeException và Error dẫn tới rollback; checked Exception mặc định không. Đừng suy ra cứ có stacktrace thì DB rollback.

**Thực hành / kết quả:** checkedCommitsByDefault ném Exception nhưng row còn; runtimeRollsBack không còn row.

## 17. catch/swallow exception

Nếu application catch lỗi trước khi proxy thấy nó, proxy có thể commit. Nhưng lỗi từ inner transactional bean hoặc JPA provider có thể đã đánh dấu rollback-only; catch không xóa marker đó.

**Thực hành / kết quả:** So sánh swallowedApplicationExceptionCommits và swallowedInnerRequiredStillPoisonsOuterTransaction.

## 18. rollbackFor / noRollbackFor

Chọn exception type theo business semantics. rollbackFor=Exception.class đổi checked failure; noRollbackFor cho một runtime type giữ commit. Quy tắc cụ thể nhất thắng; ưu tiên class thay vì tên string dễ match nhầm.

**Thực hành / kết quả:** Chạy rollbackForOverridesDefault và noRollbackForOverridesDefault. Đổi loại exception ném ra rồi dự đoán.

## 19. rollbackOnly

setRollbackOnly yêu cầu hủy khi scope hoàn tất. Ở outer scope tự quyết định rollback, method có thể return bình thường. Không có API hợp lệ để biến một transaction đã hỏng thành an toàn chỉ bằng catch.

**Thực hành / kết quả:** explicitLocalRollbackOnlyReturnsNormally: không exception nhưng không có row. Dùng TransactionTemplate nếu muốn nhận status trực tiếp.

## 20. UnexpectedRollbackException

Inner REQUIRED lỗi qua proxy đánh dấu transaction dùng chung. Outer catch rồi định commit, manager báo rằng kết quả thực tế là rollback. Đây là tín hiệu caller không được tin rằng đã commit.

**Thực hành / kết quả:** BoundaryLab.unexpectedRollback phải ném UnexpectedRollbackException và cả outer/inner đều mất. Đổi inner thành REQUIRES_NEW rồi giải thích khác biệt.

## Checkpoint

Không nhìn code, giải thích được kết quả từng test và chỉ ra ai tạo transaction, ai commit, database giữ lại gì. Ghi một dự đoán sai và nguyên nhân vào WORKBOOK.md.
