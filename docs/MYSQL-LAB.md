# MySQL lab — hai session độc lập

Chỉ chạy trong database `txlab` riêng của Docker Compose này. Không dùng database chứa dữ liệu thật.
Đây là lab InnoDB 8.0; H2 không thay thế các thí nghiệm này.

```sh
docker compose up -d --wait
```

Mở hai terminal A và B, mỗi terminal chạy:

```sh
docker compose exec db mysql -utxlab -ptxlab txlab
```

Mật khẩu trên chỉ là credential local của lab. Trong A, tạo fixture trước khi mở transaction:

```sql
CREATE TABLE IF NOT EXISTS concurrency_account (
  id INT PRIMARY KEY, balance INT NOT NULL
) ENGINE=InnoDB;
DELETE FROM concurrency_account;
INSERT INTO concurrency_account VALUES (1,1000),(2,1000);
SELECT @@transaction_isolation;
```

Trước **mỗi** thí nghiệm: `ROLLBACK;` ở cả hai terminal, rồi reset fixture trong A bằng DELETE/INSERT ở trên.
Đừng chạy suite hay app đồng thời vì test cũng reset bảng này.

## 35. Dirty read

Thực hiện theo thứ tự A1 → B1 → A2 → B2 → A3.

**A1**

```sql
SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
```

**B1**

```sql
START TRANSACTION;
UPDATE concurrency_account SET balance=900 WHERE id=1;
```

**A2**

```sql
SELECT balance FROM concurrency_account WHERE id=1; -- 900 chưa commit
```

**B2**: `ROLLBACK;`

**A3**

```sql
SELECT balance FROM concurrency_account WHERE id=1; -- 1000
ROLLBACK;
```

Giá trị 900 chưa từng được B commit. Đổi A thành READ COMMITTED, đọc thường sẽ không thấy 900 chưa commit.

## 36. Non-repeatable read

**A1**

```sql
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
START TRANSACTION;
SELECT balance FROM concurrency_account WHERE id=1; -- 1000
```

**B1**

```sql
START TRANSACTION;
UPDATE concurrency_account SET balance=900 WHERE id=1;
COMMIT;
```

**A2**

```sql
SELECT balance FROM concurrency_account WHERE id=1; -- 900
ROLLBACK;
```

Reset rồi chạy lại A ở REPEATABLE READ: lần đọc thường thứ hai vẫn 1000 theo snapshot.
Đừng thay query thành FOR UPDATE khi đang so sánh consistent reads.

## 37. Phantom read

**A1**

```sql
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
START TRANSACTION;
SELECT COUNT(*) FROM concurrency_account WHERE balance>=1000; -- 2
```

**B1**

```sql
START TRANSACTION;
INSERT INTO concurrency_account VALUES (3,1000);
COMMIT;
```

**A2**

```sql
SELECT COUNT(*) FROM concurrency_account WHERE balance>=1000; -- 3
ROLLBACK;
```

Reset và thử REPEATABLE READ: count vẫn 2 trong snapshot của A. InnoDB locking reads và next-key/gap locks
có quy tắc khác consistent reads; không kết luận mọi RR database đều xử lý phantom giống nhau.

## 38. Lost update

Đặt cả hai session READ COMMITTED rồi START TRANSACTION.
A và B đều `SELECT balance FROM concurrency_account WHERE id=1;` → 1000.
A tính debit 100, B tính debit 200 từ cùng giá trị cũ.

**A**

```sql
UPDATE concurrency_account SET balance=900 WHERE id=1;
COMMIT;
```

**B**

```sql
UPDATE concurrency_account SET balance=800 WHERE id=1;
COMMIT;
SELECT balance FROM concurrency_account WHERE id=1; -- 800, đúng ra 700
```

Thử lại bằng atomic `SET balance=balance-100` và `SET balance=balance-200`.
Với business logic phức tạp, so sánh conditional update, version checking và pessimistic lock.

## 40. Pessimistic lock

**A**

```sql
START TRANSACTION;
SELECT * FROM concurrency_account WHERE id=1 FOR UPDATE;
```

**B**

```sql
START TRANSACTION;
SELECT * FROM concurrency_account WHERE id=1 FOR UPDATE; -- chờ A
```

A `COMMIT;` thì B nhận row. B `ROLLBACK;` để kết thúc.
Đọc thường có thể dùng MVCC nên không bị chặn giống FOR UPDATE.

## 41. Deadlock

Reset. Thực hiện A1 → B1 → A2 (đang chờ) → B2.

**A1**

```sql
START TRANSACTION;
UPDATE concurrency_account SET balance=balance-1 WHERE id=1;
```

**B1**

```sql
START TRANSACTION;
UPDATE concurrency_account SET balance=balance-1 WHERE id=2;
```

**A2**

```sql
UPDATE concurrency_account SET balance=balance-1 WHERE id=2;
```

**B2**

```sql
UPDATE concurrency_account SET balance=balance-1 WHERE id=1;
```

Một session nhận error **1213**. Không giả định luôn là B. Session còn lại chạy tiếp; commit session thắng,
rollback session bị chọn rồi thử transaction đó từ đầu. Cuối lab `ROLLBACK;` ở cả hai session.

## Tự động hóa cùng behavior

```sh
./mvnw -Dspring.profiles.active=mysql -Dtest=Phase06MySqlTest test
```

Class này chỉ chạy khi system property `spring.profiles.active=mysql`; mặc định H2 báo skipped.
Test kiểm tra dirty read, RC phantom, RR snapshot và deadlock victim thật. Deadlock dùng latch để phối hợp,
không dựa vào việc may mắn hai thread chạy cùng lúc.
