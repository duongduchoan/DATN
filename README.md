# QLPT - Ứng Dụng Quản Lý Phòng Trọ (DATN)

Dự án Đồ án tốt nghiệp: Xây dựng ứng dụng di động Quản lý phòng trọ trên nền tảng Android.

## 📱 Giới thiệu
Ứng dụng hỗ trợ kết nối và quản lý giữa Chủ trọ, Người thuê trọ và Quản trị viên, giúp tối ưu hóa quy trình tìm phòng, quản lý hợp đồng, hóa đơn và yêu cầu sự cố.

## 🛠️ Công nghệ sử dụng
- **Ngôn ngữ:** Java
- **Nền tảng:** Android SDK
- **Backend / Database:** Firebase (Authentication, Cloud Firestore, Firebase Storage)
- **Kiến trúc:** MVC / Component-based Android Architecture

## 🌟 Tính năng chính
- **Xác thực & Phân quyền:** Đăng ký, đăng nhập, phân quyền người dùng (Chủ trọ, Khách thuê, Admin).
- **Dành cho Người thuê (Tenant):**
  - Tìm kiếm phòng trọ, xem chi tiết phòng.
  - Đặt lịch hẹn xem phòng, gửi yêu cầu thuê phòng.
  - Quản lý phòng đang thuê, xem hợp đồng và hóa đơn điện nước/tiền phòng.
  - Gửi và theo dõi các yêu cầu phản ánh / báo cáo sự cố phòng trọ.
- **Dành cho Chủ trọ (Landlord):**
  - Quản lý danh sách nhà trọ và phòng trọ (thêm, sửa, xóa, cập nhật trạng thái phòng).
  - Duyệt yêu cầu thuê phòng từ khách.
  - Tạo và quản lý hợp đồng thuê phòng.
  - Lập hóa đơn hàng tháng, quản lý thông tin thanh toán ngân hàng.
  - Tiếp nhận và xử lý sự cố từ người thuê.
- **Dành cho Quản trị viên (Admin):**
  - Quản lý tài khoản và xét duyệt các yêu cầu đăng ký quyền Chủ trọ.

## 🚀 Hướng dẫn cài đặt & Chạy ứng dụng
1. Clone repository về máy:
   ```bash
   git clone https://github.com/duongduchoan/DATN.git
   ```
2. Mở dự án bằng **Android Studio**.
3. Chờ Gradle đồng bộ các thư viện (Gradle Sync).
4. Cấu hình file `google-services.json` (nếu cần thay đổi dự án Firebase).
5. Chọn thiết bị ảo (Emulator) hoặc máy thật và nhấn **Run** (Shift + F10).
