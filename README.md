# PolyCoffee — Hệ thống Quản lý Quán Cà Phê (JAV102)

Ứng dụng web quản lý quán cà phê **PolyCoffee** (Dự án Java 1 — JAV102), xây dựng theo mô hình **MVC** sử dụng **Java Servlet/JSP, JSTL 3.0, JDBC, Supabase (PostgreSQL)** và quản lý thư viện bằng **Maven**.

---

## 1. Công nghệ & Thư viện sử dụng

| Thành phần | Công nghệ / Phiên bản |
| :--- | :--- |
| **Ngôn ngữ** | Java (**JDK 25**) |
| **Backend** | Jakarta Servlet 6.1, JDBC thuần |
| **Frontend** | HTML5, CSS3, JavaScript, JSP + Jakarta JSTL 3.0 (`3.0.0` / `3.0.1`) |
| **Web Server** | **Apache Tomcat v11.0** |
| **Cơ sở dữ liệu** | **Supabase (PostgreSQL Cloud)** — Driver `org.postgresql:postgresql 42.7.4` |
| **Build Tool** | **Apache Maven** (`pom.xml`) |
| **Hỗ trợ mã nguồn** | **Project Lombok `1.18.48`** |
| **IDE / AI** | Eclipse IDE for Enterprise Java and Web Developers + **Google Antigravity** |

---

## 2. Cấu trúc dự án & Danh sách các file hiện có

```text
PolyCoffee/
├── pom.xml                                   # Quản lý dependencies (PostgreSQL, Lombok, Servlet 6.1, JSTL 3.0)
├── README.md                                 # Tài liệu hướng dẫn cấu hình, DB & quy tắc làm việc nhóm
└── src/main/
    ├── java/
    │   ├── controller/                       # Các Servlet xử lý request & điều hướng JSP (thành viên phát triển)
    │   ├── DAO/
    │   │   └── CrudDAO.java                  # Interface CRUD gốc (create, update, delete, count, findAll, findById, findBySql)
    │   ├── entity/                           # 17 lớp Entity ánh xạ 1-1 với 17 bảng trên Supabase (dùng Lombok)
    │   │   ├── Role.java                     # Bảng roles — Vai trò phân quyền (ADMIN, STAFF, CUSTOMER)
    │   │   ├── User.java                     # Bảng users — Tài khoản nhân viên & khách hàng, điểm tích lũy
    │   │   ├── Shift.java                    # Bảng shifts — Ca làm việc & đối soát tiền mặt đầu/cuối ca
    │   │   ├── Category.java                 # Bảng categories — Danh mục nhóm đồ uống/món ăn
    │   │   ├── Product.java                  # Bảng products — Thông tin chung sản phẩm
    │   │   ├── ProductSize.java              # Bảng product_sizes — Giá bán theo kích cỡ (S, M, L)
    │   │   ├── Topping.java                  # Bảng toppings — Danh sách topping bán kèm & liên kết nguyên liệu
    │   │   ├── Ingredient.java               # Bảng ingredients — Kho nguyên liệu & định mức tồn tối thiểu
    │   │   ├── Recipe.java                   # Bảng recipes — Định lượng nguyên liệu pha chế theo từng size món
    │   │   ├── InventoryTransaction.java     # Bảng inventory_transactions — Lịch sử nhập/xuất/hao hụt kho
    │   │   ├── Pager.java                    # Bảng pagers — Thẻ rung gọi món tại quầy
    │   │   ├── Order.java                    # Bảng orders — Thông tin hóa đơn (POS & Online)
    │   │   ├── OrderItem.java                # Bảng order_items — Chi tiết từng món, size, đường, đá trong đơn
    │   │   ├── OrderItemTopping.java         # Bảng order_item_toppings — Topping kèm theo từng ly nước
    │   │   ├── Voucher.java                  # Bảng vouchers — Mã giảm giá & điều kiện áp dụng
    │   │   ├── OrderVoucher.java             # Bảng order_vouchers — Lịch sử áp dụng voucher cho đơn hàng
    │   │   └── Review.java                   # Bảng reviews — Đánh giá sao & bình luận sản phẩm đã mua
    │   └── utils/                            # Bộ tiện ích dùng chung toàn hệ thống (đã cấu hình sẵn)
    │       ├── JdbcUtil.java                 # Kết nối Supabase (PostgreSQL) qua JDBC & thực thi SQL
    │       ├── ParamUtil.java                # Đọc & ép kiểu tham số từ form an toàn (String, int, double, boolean, Date)
    │       ├── FileUtil.java                 # Upload, validate định dạng và xóa file ảnh
    │       ├── DateUtil.java                 # Chuyển đổi, định dạng ngày giờ & kiểu SQL Date/Timestamp
    │       ├── AuthUtil.java                 # Quản lý phiên đăng nhập (HttpSession) & kiểm tra quyền
    │       ├── AuthFilter.java               # Chặn truy cập chưa đăng nhập / sai quyền (/admin/*, /staff/*, /pos/*, /bills/*)
    │       └── Utf8Filter.java               # Thiết lập bảng mã UTF-8 toàn cục cho tiếng Việt
    └── webapp/
        ├── META-INF/
        │   └── MANIFEST.MF
        └── WEB-INF/
            └── web.xml                       # Cấu hình ứng dụng web
```

---

## 3. Thông tin Cơ sở dữ liệu (Supabase) & Phương thức kết nối

Dự án sử dụng **Supabase (PostgreSQL trên Cloud)** làm cơ sở dữ liệu dùng chung cho cả nhóm — kết nối trực tiếp từ Backend Java qua **JDBC thuần** (không dùng Data API).

> **Ưu điểm cho thành viên:** Bạn **KHÔNG cần cài đặt SQL Server, PostgreSQL hay import file `.sql`** về máy cá nhân. Chỉ cần máy có **Internet**, `git pull` code về và cập nhật Maven là kết nối chung Database với cả nhóm ngay.

### 3.1. Thông số kết nối (`src/main/java/utils/JdbcUtil.java`)
* **Hệ quản trị:** PostgreSQL (Supabase — Region `Southeast Asia / Singapore`)
* **Phương thức kết nối:** **Session Pooler (IPv4, cổng `5432`)** bắt buộc mã hóa SSL (`?sslmode=require`)
* **JDBC Driver:** `org.postgresql.Driver`
* **Host:** `aws-1-ap-southeast-1.pooler.supabase.com`
* **Port:** `5432`
* **Database:** `postgres`
* **Username:** `postgres.uwxirktukqfmwykzvkbe`
* **Connection URL:**
  ```text
  jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require
  ```
* **Password:** Liên hệ nhóm trưởng (hoặc xem trực tiếp trong `JdbcUtil.java` nếu repo ở chế độ Private).

### 3.2. Lưu ý bắt buộc khi viết câu lệnh SQL trong DAO (PostgreSQL)
Vì DB chạy **PostgreSQL** (khác một số từ khóa so với SQL Server), khi viết SQL trong các lớp DAO cần tuân thủ:
1. **Lấy N dòng đầu / Phân trang:** Dùng `LIMIT 10 OFFSET 0` (không dùng `TOP 10`).
2. **Tìm kiếm tiếng Việt không phân biệt hoa/thường:** Dùng **`ILIKE`** (ví dụ: `WHERE name ILIKE ?`) vì `LIKE` trong PostgreSQL có phân biệt hoa/thường.
3. **Chuỗi tiếng Việt:** Viết `'Cà phê sữa'` bình thường, **không** thêm chữ `N` phía trước (`N'...'`).
4. **Ngày giờ hiện tại:** Dùng `NOW()` hoặc `CURRENT_TIMESTAMP` (không dùng `GETDATE()`).
5. **Kiểu Boolean:** Dùng `TRUE` / `FALSE` (không dùng `1` / `0` trong điều kiện `WHERE is_active = TRUE`).
6. **Giải phóng kết nối:** Khi gọi `JdbcUtil.executeQuery(...)` ở DAO, sau khi đọc xong `ResultSet rs` nhớ đóng kết nối (`rs.getStatement().getConnection().close()`) để tránh quá tải giới hạn kết nối của gói Free Supabase.
7. **Bảo vệ dữ liệu chung:** Không tự ý chạy lệnh `DROP TABLE`, `TRUNCATE` hoặc xóa sạch dữ liệu test của nhóm mà chưa báo trước.

---

## 4. Yêu cầu môi trường & Cài đặt lần đầu

Toàn bộ thành viên cài đặt thống nhất:
1. **JDK 25** (`JavaSE-25`).
2. **Apache Tomcat v11.0** *(trong Eclipse phải đặt tên Server Runtime chính xác là **`Apache Tomcat v11.0`**)*.
3. **Eclipse IDE for Enterprise Java and Web Developers** (hỗ trợ JDK 25 & Tomcat 11).
4. **Lombok `1.18.48`** đã tích hợp vào Eclipse.

### Các bước thiết lập sau khi Clone
1. **Clone dự án:**
   ```bash
   git clone <link-repo-github-cua-nhom>
   ```
2. **Cài đặt Lombok `1.18.48` vào Eclipse (Bắt buộc):**
   * Tải `lombok-1.18.48.jar` tại https://projectlombok.org/download (hoặc lấy trong `.m2/repository/org/projectlombok/lombok/1.18.48/`).
   * Chạy file `.jar` (`java -jar lombok-1.18.48.jar`), trỏ tới `eclipse.exe` → **Install / Update** → **Khởi động lại Eclipse**.
3. **Khai báo JDK 25 & Apache Tomcat v11.0 trong Eclipse:**
   * **JDK:** *Window → Preferences → Java → Installed JREs* → chọn **JDK 25** làm mặc định.
   * **Tomcat:** *Window → Preferences → Server → Runtime Environments* → thêm **`Apache Tomcat v11.0`** dùng JRE **JDK 25**.
4. **Import dự án Maven vào Eclipse:**
   * *File → Import... → Maven → Existing Maven Projects* → chọn thư mục `PolyCoffee` → **Finish**.
   * Chuột phải project **`PolyCoffee`** → **Maven → Update Project...** (`Alt + F5`) → **OK** để Maven tải driver PostgreSQL và các thư viện.
5. **Kiểm tra kết nối Supabase:**
   * Mở `src/main/java/utils/JdbcUtil.java` → chuột phải → **Run As → Java Application** (nếu đã có hàm `main` test) để xác nhận kết nối thành công.
6. **Chạy Web Server:**
   * Chuột phải project **`PolyCoffee`** → **Run As → Run on Server** → chọn **Apache Tomcat v11.0** → truy cập `http://localhost:8080/PolyCoffee/`.

---

## 5. Hướng dẫn tiếp tục phát triển dự án với Antigravity (Dành cho thành viên)

> [!CAUTION]
> **QUY TẮC BẮT BUỘC DÀNH CHO AI (ANTIGRAVITY / AGENT) TRÊN MÁY THÀNH VIÊN:**  
> **KHÔNG ĐƯỢC tự động chỉnh sửa, đổi tên, tái cấu trúc (refactor) hoặc xóa bất kỳ file/đoạn code có sẵn nào đã được bàn giao** (bao gồm `pom.xml`, `src/main/java/utils/*`, `src/main/java/DAO/CrudDAO.java`, toàn bộ 17 file trong `src/main/java/entity/*`, `web.xml` và cấu trúc bảng trên Supabase) **khi chưa hỏi ý kiến và chưa được người dùng đồng ý rõ ràng.**  
> Nếu phát hiện lỗi hoặc cần thay đổi các file nền tảng này để phục vụ tính năng mới, AI **bắt buộc phải giải thích rõ lý do, đề xuất phương án và chờ người dùng xác nhận** trước khi sửa.

### Quy trình làm việc hiệu quả với Antigravity:
1. **Mở đúng thư mục workspace:** Mở trực tiếp thư mục chứa `PolyCoffee` trong Antigravity.
2. **Tạo nhánh Git riêng trước khi nhờ AI code:**
   ```bash
   git checkout -b feat/<ten-phan-he-cua-ban>
   # Ví dụ: feat/menu, feat/order, feat/auth, feat/inventory
   ```
3. **Prompt chuẩn khi bắt đầu phiên làm việc với Antigravity:**
   > *"Hãy đọc `README.md`, `CrudDAO.java`, `JdbcUtil.java` và các file liên quan trong `src/main/java/entity/`. Tôi phụ trách phân hệ **[tên phân hệ]**. Lưu ý: KHÔNG tự ý sửa các file có sẵn đã bàn giao nếu chưa hỏi tôi. Hãy giúp tôi viết DAO, Servlet và JSP cho chức năng [mô tả chức năng] theo đúng chuẩn Jakarta Servlet 6.1, JDBC và PostgreSQL."*
4. **Kiểm tra trước khi commit/push:**
   * Chạy thử trên Tomcat 11 ở máy cá nhân.
   * Chạy `git status` để chắc chắn chỉ thay đổi các file thuộc phạm vi phân hệ của mình, không vô tình sửa `JdbcUtil.java` hay file của thành viên khác.

---

## 6. Quy tắc làm việc nhóm trên GitHub

* **Chia việc theo phân hệ độc lập** (mỗi người làm trọn bộ `DAO` + `Servlet` + `JSP` của phân hệ mình để tránh conflict):
  1. **Auth & Ca làm việc:** `Role`, `User`, `Shift`
  2. **Thực đơn (Menu):** `Category`, `Product`, `ProductSize`, `Topping`
  3. **Bán hàng & Đơn hàng:** `Order`, `OrderItem`, `OrderItemTopping`, `Pager`
  4. **Khuyến mãi, Đánh giá & Báo cáo:** `Voucher`, `OrderVoucher`, `Review`
  5. **Kho & Công thức:** `Ingredient`, `Recipe`, `InventoryTransaction`
* **Quản lý thư viện:** Mọi thư viện mới phải khai báo trong `pom.xml` (không dùng *Add External JARs...*).
* **Chuẩn Servlet:** Luôn import **`jakarta.servlet.*`** (không dùng `javax.servlet.*`).
* **Encoding tiếng Việt:** Đầu mỗi file `.jsp` khai báo:
  ```jsp
  <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
  ```
* **Không commit file rác:** Không push thư mục `target/`, `build/`, hoặc chỉnh sửa đường dẫn cá nhân trong `.classpath` / `.settings/`.

---

## 7. Xử lý một số lỗi thường gặp

| Lỗi gặp phải | Nguyên nhân & Cách khắc phục |
| :--- | :--- |
| Eclipse báo lỗi không thấy `getXxx()`, `setXxx()`, `isActive()` | Chưa cài `lombok-1.18.48.jar` vào Eclipse hoặc chưa restart Eclipse. |
| Lỗi `The import jakarta cannot be resolved` | Chưa gắn `Apache Tomcat v11.0` hoặc chưa chạy `Maven → Update Project` (`Alt + F5`). |
| `ClassNotFoundException: org.postgresql.Driver` | Chưa chạy `Maven → Update Project` (`Alt + F5`) để tải driver PostgreSQL từ `pom.xml`. |
| `FATAL: password authentication failed` | Sai mật khẩu DB trong `JdbcUtil.java` (kiểm tra lại mật khẩu Supabase với nhóm trưởng). |
| Không kết nối được Supabase / timeout | Kiểm tra kết nối Internet, đảm bảo dùng **Session pooler (cổng `5432`)** và có `?sslmode=require`. Nếu sau 7 ngày không có truy vấn, gói Free Supabase sẽ tự Pause → nhóm trưởng vào Dashboard bấm **Restore**. |
| Lỗi upload ảnh (`IllegalStateException`) | Servlet xử lý upload phải có `@MultipartConfig` và form JSP phải có `enctype="multipart/form-data"`. |
