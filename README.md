<div align="center">

# 📸 Photo-Management

**Ứng dụng Android quản lý dịch vụ in ảnh / in ấn theo diện tích (m²)**

Quản lý Khách hàng · Dịch vụ in · Đơn in · Tìm kiếm & thống kê — toàn bộ dữ liệu lưu cục bộ bằng SQLite.

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Java-orange?logo=openjdk&logoColor=white)
![UI](https://img.shields.io/badge/UI-Material%203-7C3AED)
![minSdk](https://img.shields.io/badge/minSdk-26-blue)
![Storage](https://img.shields.io/badge/Storage-SQLite-003B57?logo=sqlite&logoColor=white)

</div>

---

## 🧭 Giới thiệu

**Photo-Management** là ứng dụng Android (Java thuần, không dùng Kotlin/Jetpack Compose) dành cho cửa hàng/dịch vụ **in ấn tính tiền theo diện tích (m²)**. Ứng dụng cho phép:

- Quản lý **Khách hàng** (mã tự sinh, chống trùng SĐT)
- Quản lý **Dịch vụ in** (loại in × khổ giấy × giá/m²)
- Lập **Đơn in** với **tự động tính thành tiền** theo thời gian thực
- **Tìm kiếm / lọc** đơn in theo nhiều điều kiện kèm **thống kê tổng quan**

Giao diện theo chuẩn **Material Design 3**, hỗ trợ **Dark Mode**.

## 📱 Ảnh màn hình

> Thả ảnh chụp vào thư mục `docs/screenshots/` đúng tên file bên dưới là ảnh tự hiện.

| Trang chủ | Đơn in | Tìm kiếm / Lọc |
|:---:|:---:|:---:|
| ![Dashboard](docs/screenshots/dashboard.png) | ![Đơn in](docs/screenshots/order.png) | ![Tìm kiếm](docs/screenshots/search.png) |

| Khách hàng | Dịch vụ in | Dark Mode |
|:---:|:---:|:---:|
| ![Khách hàng](docs/screenshots/customer.png) | ![Dịch vụ](docs/screenshots/service.png) | ![Dark Mode](docs/screenshots/dark_mode.png) |

## ✨ Tính năng

| Nhóm | Chi tiết |
|------|----------|
| 👥 **Khách hàng** | Thêm/sửa/xoá, mã tự sinh `KH001…`, kiểm tra SĐT (9–11 số), chặn trùng SĐT, chặn xoá khi đang có đơn |
| 🖨️ **Dịch vụ in** | Thêm/sửa/xoá, mã tự sinh `DV001…`, chọn loại in & khổ giấy bằng *segmented control*, chặn trùng cặp (loại in, khổ), chặn xoá khi đang có đơn |
| 🧾 **Đơn in** | Mã tự sinh `DH001…`, chọn khách & dịch vụ, **tự tính** `thành tiền = giá/m² × diện tích × số lượng`, chọn ngày bằng *date picker* |
| 🔍 **Tìm kiếm / Lọc** | Lọc động theo **khách hàng · khổ giấy · loại in · mức tiền tối thiểu**, tự lọc khi đổi điều kiện, **card thống kê** (số đơn · doanh thu · diện tích) |
| 🎨 **Giao diện** | Material 3, dashboard dạng lưới, **bottom navigation** dạng pill, **empty state**, Dark Mode |

## 🛠️ Công nghệ

- **Ngôn ngữ:** Java 11
- **Giao diện:** XML layout + RecyclerView + Material Components (Material 3)
- **Lưu trữ:** SQLite qua `SQLiteOpenHelper` (không backend/mạng)
- **Build:** Gradle (Kotlin DSL) + version catalog
- **minSdk:** 26 · **targetSdk / compileSdk:** 36

## 🏗️ Kiến trúc

Mẫu đơn giản: **Activity → DatabaseHelper → SQLite** (không Repository/ViewModel, mỗi màn hình là 1 Activity).

```
app/src/main/java/com/example/photo_management/
├── activities/   # Màn hình (Main, Customer, Service, Order, SearchOrder)
├── adapters/     # RecyclerView.Adapter
├── models/       # POJO: Customer, PrintService, PrintOrder, OrderDetail
├── database/     # DBContract (schema) + DatabaseHelper (toàn bộ SQLite)
└── utils/        # ValidationUtils, NavBar
```

### Cơ sở dữ liệu (`print_service_db`)

| Bảng | Cột chính |
|------|-----------|
| `customers` | `code` (UNIQUE), `name`, `phone` |
| `print_services` | `code` (UNIQUE), `print_type`, `size`, `price_per_m2` |
| `print_orders` | `code` (UNIQUE), `customer_id` (FK), `service_id` (FK), `area`, `quantity`, `total_price`, `order_date`, `note` |

## 🚀 Cài đặt & Chạy

Yêu cầu: **Android Studio** (mới) + **JDK 11+** + Android SDK 36.

```bash
# Clone
git clone https://github.com/lvthe/photo_management.git
cd photo_management
```

Mở bằng Android Studio rồi bấm **Run ▶**, hoặc dùng dòng lệnh:

```powershell
# Build APK debug
.\gradlew.bat assembleDebug

# Cài lên thiết bị/emulator đang kết nối
.\gradlew.bat installDebug
```

> File `local.properties` (chứa `sdk.dir`) sẽ được Android Studio tự tạo khi mở dự án.

## 📂 Quy ước phát triển

- Mọi truy vấn/CRUD đặt trong `DatabaseHelper`; tên bảng/cột lấy từ hằng số `DBContract`.
- Sinh mã theo mẫu `generateNextXxxCode()` (tiền tố 2 ký tự + 3 chữ số).
- Ràng buộc nghiệp vụ (trùng SĐT, trùng dịch vụ, chặn xoá) kiểm ở tầng app.
- Validation & định dạng tiền qua `ValidationUtils` (locale `vi-VN`).

## 📄 License

Dự án phục vụ mục đích học tập.
