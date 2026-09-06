# Workbook

Copy mẫu này cho mỗi lab. Dành 30–45 phút mỗi buổi; học theo phase, không cần hoàn thành cả 55 mục một lần.

## Lab / topic số ...

- Dự đoán: DB sẽ còn row nào? Số dư bao nhiêu? Exception gì, tại dòng nào?
- Boundary: external call nào đi qua proxy? Manager nào? Bao nhiêu physical transaction/connection?
- Quan sát: test result, dòng log quan trọng, SQL và trạng thái sau cùng.
- Giải thích: tại sao kết quả như vậy?
- Biến thể: đổi annotation/catch/caller và chạy lại.
- Bài học áp dụng: use case nào nên hoặc không nên dùng?

## Tự kiểm tra cuối khóa

1. Annotation đúng nhưng self-invocation: tại sao transaction không được tạo?
2. Checked exception thoát khỏi service: tại sao row vẫn tồn tại?
3. Catch inner REQUIRED: tại sao outer vẫn rollback?
4. Audit REQUIRES_NEW: tại sao audit còn dù payment fail? Có làm cạn pool không?
5. Flush thành công có chứng minh commit thành công không?
6. Tại sao hai @Transactional method vẫn có thể lost update?
7. Gửi email trong transaction có được thu hồi khi rollback không?
8. Crash sau outbox send: ai phải xử lý duplicate?
9. Saga compensation khác DB rollback ở điểm nào?
10. Một retry đúng phải bao ngoài bước nào để retry được lỗi commit?
