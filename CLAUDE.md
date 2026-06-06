# CLAUDE.md

Tài liệu hướng dẫn cho Claude Code (claude.ai/code) khi làm việc với mã nguồn trong repo này.

## Tổng quan

**Photo-Management** là ứng dụng Android (Java thuần, không dùng Kotlin/Jetpack Compose) quản lý **dịch vụ in ảnh / in ấn theo diện tích (m²)**. Ứng dụng cho phép quản lý Khách hàng, Dịch vụ in, lập Đơn in, và tìm kiếm/lọc đơn in theo nhiều điều kiện (khách hàng, khổ giấy, loại in, mức tiền). Toàn bộ dữ liệu lưu cục bộ bằng **SQLite** (không có backend/mạng).

- **Ngôn ngữ:** Java
- **Giao diện:** XML layout + RecyclerView + AlertDialog (không dùng Fragment, mỗi màn hình là 1 Activity)
- **Lưu trữ:** SQLite qua `SQLiteOpenHelper`
- **Package gốc:** `com.example.photo_management`
- **applicationId:** `com.example.photo_management`
- **minSdk:** 26 · **targetSdk / compileSdk:** 36 · **Java:** 11
- **versionName:** 1.0

## Build & Run

Dự án dùng Gradle (Kotlin DSL) với version catalog (`gradle/libs.versions.toml`).

```powershell
# Build debug APK
.\gradlew.bat assembleDebug

# Cài lên thiết bị/emulator đang kết nối
.\gradlew.bat installDebug

# Chạy unit test
.\gradlew.bat test

# Chạy instrumented test (cần thiết bị/emulator)
.\gradlew.bat connectedAndroidTest

# Build sạch
.\gradlew.bat clean
```

> Lưu ý: Đây **không phải** git repository. Mở bằng Android Studio (có sẵn `.idea/`). `local.properties` chứa `sdk.dir`.

## Kiến trúc & Cấu trúc thư mục

Mã nguồn tại `app/src/main/java/com/example/photo_management/`, chia theo lớp chức năng:

```
activities/   – Các màn hình (mỗi Activity = 1 chức năng chính)
adapters/     – RecyclerView.Adapter cho danh sách
models/       – POJO (Customer, PrintService, PrintOrder, OrderDetail)
database/     – DBContract (schema constants) + DatabaseHelper (toàn bộ logic SQLite)
utils/        – ValidationUtils (kiểm tra dữ liệu, format tiền tệ)
```

Mẫu kiến trúc: **Activity → DatabaseHelper → SQLite**. Không có Repository/ViewModel; mỗi Activity tự khởi tạo `new DatabaseHelper(this)` và gọi trực tiếp. Adapter giao tiếp ngược về Activity qua interface listener (`OnXxxActionListener`).

### Mô hình dữ liệu (3 bảng)

DB: `print_service_db`, version `1`. Khai báo cột tại [DBContract.java](app/src/main/java/com/example/photo_management/database/DBContract.java); tạo bảng + CRUD tại [DatabaseHelper.java](app/src/main/java/com/example/photo_management/database/DatabaseHelper.java).

| Bảng | Tên SQLite | Cột chính | Ghi chú |
|------|-----------|-----------|---------|
| Khách hàng | `customers` | `code` (UNIQUE), `name`, `phone` | `phone` được kiểm tra trùng ở tầng app |
| Dịch vụ in | `print_services` | `code` (UNIQUE), `print_type`, `size`, `price_per_m2` | cặp (`print_type`,`size`) là duy nhất ở tầng app |
| Đơn in | `print_orders` | `code` (UNIQUE), `customer_id` (FK), `service_id` (FK), `area`, `quantity`, `total_price`, `order_date`, `note` | FK tới `customers` và `print_services` |

- `print_type`: `"Mau"` (màu) hoặc `"Den trang"` (đen trắng)
- `size`: `"A3"`, `"A4"`, `"A5"`
- **`OrderDetail`** là model chỉ-đọc (không map 1-1 với bảng) dùng cho kết quả JOIN 3 bảng để hiển thị (tên khách + loại in + size + ...).

> `onUpgrade` hiện **DROP toàn bộ bảng** rồi tạo lại — tăng `DATABASE_VERSION` sẽ **xóa sạch dữ liệu**. Lưu ý khi đổi schema.

## Các chức năng chính

Điểm vào: [MainActivity.java](app/src/main/java/com/example/photo_management/activities/MainActivity.java) — màn hình dashboard với 4 nút điều hướng tới 4 Activity chức năng. `MainActivity` là activity duy nhất `exported=true` (LAUNCHER).

> **Điều hướng:** 4 màn chức năng dùng chung 1 **BottomNavigationView** (pill nổi ở đáy), wiring qua [NavBar.java](app/src/main/java/com/example/photo_management/utils/NavBar.java) — mỗi Activity gọi `NavBar.setup(this, bottomNav, R.id.nav_xxx)`. Chuyển tab = `startActivity` với cờ `SINGLE_TOP | REORDER_TO_FRONT` (các Activity khai báo `launchMode="singleTop"`), không animation. `MainActivity` là hub, không gắn bottom nav.

### 1. Quản lý Khách hàng — [CustomerActivity](app/src/main/java/com/example/photo_management/activities/CustomerActivity.java)
- Danh sách (RecyclerView) + FAB thêm mới; thêm/sửa qua `dialog_customer`.
- Mã KH tự sinh (`KH001`, `KH002`, ...), không cho sửa.
- Validate: bắt buộc tên/SĐT; SĐT 9–11 chữ số; **chặn trùng SĐT** (`isPhoneExists`).
- **Không cho xóa** khách đang có đơn hàng (`customerHasOrders`).

### 2. Quản lý Dịch vụ in — [ServiceActivity](app/src/main/java/com/example/photo_management/activities/ServiceActivity.java)
- CRUD dịch vụ; mã tự sinh (`DV001`...). Chọn `print_type`/`size` bằng **segmented control** (`MaterialButtonToggleGroup`); nhập giá/m². Giá trị lưu DB đặt ở `android:tag` của nút (vd hiển thị "Màu" nhưng lưu `"Mau"`).
- **Chặn trùng** cặp (loại in, khổ giấy) qua `isServiceExists`.
- **Không cho xóa** dịch vụ đang có đơn (`serviceHasOrders`).

### 3. Lập Đơn in — [OrderActivity](app/src/main/java/com/example/photo_management/activities/OrderActivity.java)
- CRUD đơn; mã tự sinh (`DH001`...). Chọn khách & dịch vụ bằng Spinner.
- **Tự tính thành tiền:** `total_price = price_per_m2 × area × quantity`, cập nhật realtime khi đổi diện tích/số lượng/dịch vụ (`TextWatcher` + `OnItemSelectedListener`).
- Ngày đơn mặc định = hôm nay (`yyyy-MM-dd`).
- Chặn thêm đơn nếu chưa có khách hàng hoặc chưa có dịch vụ nào.
- Danh sách hiển thị dữ liệu JOIN (`getAllOrderDetails`); có nút Sửa/Xóa (xác nhận trước khi xóa).

### 4. Tìm kiếm / Lọc đơn in — [SearchOrderActivity](app/src/main/java/com/example/photo_management/activities/SearchOrderActivity.java)
- **Bộ lọc động** (chế độ chỉ-đọc), tự lọc lại ngay khi đổi bất kỳ điều kiện nào:
  - **Khách hàng**: dropdown Material (exposed menu, có mục "Tất cả khách hàng").
  - **Khổ giấy / Loại in / Thành tiền tối thiểu**: các nhóm `Chip` (single-select), giá trị query đặt ở `android:tag`.
- Gọi `searchOrders(customerId, size, printType, minPrice)` — `customerId <= 0` / chuỗi rỗng / `minPrice < 0` nghĩa là **bỏ điều kiện** đó. WHERE được build động, bind tham số an toàn.
- **Card tổng quan**: số đơn · tổng doanh thu · tổng diện tích (tính trong Java từ kết quả). Có **empty state** khi không khớp.
- Màn "Đơn theo khách hàng" cũ đã **gộp vào đây** (qua bộ lọc Khách hàng). Truy vấn cũ `searchA3ColorOrdersAbove500k` / `getOrdersByCustomer` vẫn còn trong `DatabaseHelper` nhưng không còn dùng ở UI.

## Quy ước & Mẫu code (Process / Convention)

Khi thêm/sửa tính năng, **bám theo các mẫu đang có** thay vì giới thiệu pattern mới:

- **Tầng dữ liệu:** mọi truy vấn/CRUD đặt trong `DatabaseHelper`. Tên cột/bảng **luôn** lấy từ hằng số trong `DBContract` (không hardcode chuỗi SQL rời rạc).
- **Sinh mã tự động:** theo mẫu `generateNextXxxCode()` — tiền tố 2 ký tự + số thứ tự 3 chữ số (`KH`/`DV`/`DH` + `%03d`).
- **Ràng buộc nghiệp vụ** (trùng SĐT, trùng dịch vụ, chặn xóa khi còn tham chiếu) kiểm tra ở **tầng app** qua các hàm `isXxxExists` / `xxxHasOrders`, **không** dựa vào constraint của SQLite.
- **Validation & format tiền:** luôn dùng [ValidationUtils](app/src/main/java/com/example/photo_management/utils/ValidationUtils.java) (`isValidPhone`, `isPositiveDouble/Int`, `parseDouble/Int`, `formatCurrency`). `formatCurrency` định dạng theo locale `vi-VN`, hậu tố `" đ"`.
- **Màn hình danh sách:** RecyclerView + `LinearLayoutManager`; Adapter nhận `List<Model>`, một listener interface, và cờ `showActionButtons` để bật/tắt nút sửa/xóa (xem `OrderAdapter`).
- **Thêm/Sửa:** dùng `AlertDialog` inflate layout `dialog_*`; nút "Lưu" gắn `OnClickListener` **sau khi** `dialog.show()` để tự kiểm soát việc đóng dialog khi validate fail (không gọi `dismiss()` nếu lỗi).
- **Thông báo:** dùng `Toast` cho phản hồi thao tác; `AlertDialog` cho cảnh báo chặn xóa. Toàn bộ chuỗi UI bằng **tiếng Việt** (lưu ý: `print_type` lưu DB là không dấu — `"Mau"`, `"Den trang"`).
- **Đăng ký Activity mới:** thêm vào [AndroidManifest.xml](app/src/main/AndroidManifest.xml). Activity nội bộ không cần `exported=true`.
- **Sau khi thay đổi DB:** gọi lại `loadXxx()` để refresh RecyclerView (clear list → addAll → `notifyDataSetChanged`).

## Resource layout (app/src/main/res/layout)

- `activity_*` — màn hình; `dialog_*` — form thêm/sửa; `item_*` — 1 dòng trong RecyclerView.
- Tên view ID đi cùng cặp Activity/Adapter tương ứng (vd `rvOrders`, `fabAddOrder`, `tvOrderTotalPrice`).

## Test

- `app/src/test/` — unit test JVM (`ExampleUnitTest`, hiện chỉ là mẫu mặc định).
- `app/src/androidTest/` — instrumented test (`ExampleInstrumentedTest`).
- Hiện **chưa có test nghiệp vụ thực sự**; khi viết test nên ưu tiên logic trong `DatabaseHelper` và `ValidationUtils`.
