# Phase 2 — Spring machinery

Chạy: `./mvnw -Dtest=Phase02ProxyTest test`

Mỗi mục: đọc → dự đoán → chạy → thay một yếu tố → giải thích.

## 7. @Transactional thực chất là gì?

Annotation là metadata. Bean phải được container quản lý và advice phải chặn được lời gọi. Đối tượng tự new không tự có transaction.

**Thực hành / kết quả:** So sánh lab.active() với lab.selfActive(): true / false.

## 8. Spring AOP Proxy

Caller thường giữ proxy, proxy chạy advice rồi gọi target. Thân method chạy trên target; lời gọi this.other() không quay lại proxy trong proxy mode.

**Thực hành / kết quả:** Chạy externalCallCrossesProxy. Xem lab.getClass() và AopUtils.isAopProxy(lab) trong debugger.

## 9. TransactionInterceptor

Advice đọc metadata, chọn manager, bắt đầu/tham gia transaction, gọi target, xử lý kết quả hoặc exception, rồi hoàn tất và cleanup. Lỗi commit có thể xảy ra sau khi thân method đã return.

**Thực hành / kết quả:** Đặt breakpoint invoke của TransactionInterceptor khi gọi pay qua controller. Step tới invokeWithinTransaction.

## 10. External call vào pay()

LabController nhận request rồi gọi PaymentService được inject. Đó là external bean call nên pay được advice. Controller không cần giữ transaction trong lúc parse request hoặc tạo response.

**Thực hành / kết quả:** Chạy request thành công và fail=true trong requests.http; GET accounts để kiểm chứng, đừng chỉ dựa HTTP status.

## 11. JDK Proxy vs class-based proxy

JDK proxy dựa interface; class-based proxy tạo subclass. Final method/class không thể được override để advice theo cách subclass. Private method không được proxy chặn. App này mặc định dùng class proxy; public method giúp ví dụ dễ quan sát.

**Thực hành / kết quả:** Chạy jdkAndClassBasedProxy. Đổi proxyTargetClass và quan sát type. Test này tách việc tạo proxy khỏi transaction advice.

## 12. Transaction flow

Chuỗi: caller → proxy → interceptor → manager → target → flush nếu có → commit/rollback → release resource → caller. Khi tham gia REQUIRED, inner return không có nghĩa physical commit.

**Thực hành / kết quả:** Vẽ call stack của PaymentService.pay và BoundaryLab.unexpectedRollback. Đánh dấu nơi SQL thực sự chạy, nơi exception phát sinh.

## 13. Self Invocation

selfInvocation gọi runtime cùng instance nên annotation trên runtime không được xét. Vì outer không có transaction, EntryStore auto-commit dù runtime ném lỗi. Nếu outer đã có transaction, inner vẫn chạy trong transaction outer, nhưng setting inner không được áp dụng.

**Thực hành / kết quả:** Chạy selfInvocationBypassesAdvice: exception vẫn có, row self vẫn tồn tại. Đây là test chứng minh bug, không phải behavior nên dùng.

## 14. Cách giải quyết self-invocation

Tách operation sang bean khác và inject là lựa chọn rõ ràng, như BoundaryLab → PropagationLab. TransactionTemplate là cách programmatic. AspectJ weaving là phương án khác cần setup riêng; self-injection/AopContext làm coupling tăng.

**Thực hành / kết quả:** Chạy movingCallerOutsideBeanRestoresRollback. Refactor selfInvocation sang một caller bean mới, giữ assertion row phải rollback.

## Checkpoint

Không nhìn code, giải thích được kết quả từng test và chỉ ra ai tạo transaction, ai commit, database giữ lại gì. Ghi một dự đoán sai và nguyên nhân vào WORKBOOK.md.
