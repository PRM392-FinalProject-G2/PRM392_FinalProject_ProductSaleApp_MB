# Firebase Cloud Messaging Setup Guide

## ⚠️ QUAN TRỌNG - BẠN CẦN LÀM

Để Firebase Notifications hoạt động, bạn **BẮT BUỘC** phải có file `google-services.json`.

### Bước 1: Tạo Firebase Project (nếu chưa có)

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" hoặc chọn project hiện có
3. Nhập tên project: **prm-techzone-g2** (hoặc tên khác)
4. Làm theo hướng dẫn để hoàn tất tạo project

### Bước 2: Add Android App vào Firebase Project

1. Trong Firebase Console, chọn project của bạn
2. Click biểu tượng Android (hoặc "Add app" → Android)
3. Nhập thông tin:
   - **Android package name**: `com.example.prm392_finalproject_productsaleapp_group2`
   - **App nickname**: TechZone (hoặc tên bạn muốn)
   - **Debug signing certificate SHA-1**: (optional, có thể bỏ qua)
4. Click "Register app"

### Bước 3: Download google-services.json

1. Sau khi đăng ký app, Firebase sẽ cho phép download file `google-services.json`
2. Click "Download google-services.json"
3. **Copy file này vào thư mục:**
   ```
   PRM392_FinalProject_ProductSaleApp_Group2/app/google-services.json
   ```
   
   📁 Cấu trúc đúng:
   ```
   PRM392_FinalProject_ProductSaleApp_Group2/
   ├── app/
   │   ├── google-services.json  ← ĐẶT FILE VÀO ĐÂY
   │   ├── build.gradle.kts
   │   └── src/
   ├── build.gradle.kts
   └── settings.gradle.kts
   ```

4. Click "Next" và "Continue to console"

### Bước 4: Sync Project

1. Mở Android Studio
2. Click "Sync Now" hoặc "Sync Project with Gradle Files"
3. Build project để kiểm tra: `Build > Make Project`

---

## ✅ Kiểm tra cài đặt thành công

1. Build project không có lỗi
2. File `google-services.json` tồn tại trong `app/` folder
3. Chạy app, kiểm tra Logcat với filter: `FCMService` hoặc `FCMTokenManager`
4. Bạn sẽ thấy log: `FCM Token: ...` khi app khởi động

---

## 🔥 Tính năng đã implement

### ✅ Backend (.NET)
- ✅ Firebase Admin SDK đã cài và config
- ✅ UserDeviceToken Repository & Service
- ✅ FirebaseNotificationService để gửi notification
- ✅ UserDeviceTokensController API endpoints
- ✅ CartItemsController tự động gửi notification khi cart thay đổi

### ✅ Mobile (Android)
- ✅ Firebase Cloud Messaging dependencies
- ✅ ShortcutBadger để hiển thị badge trên app icon
- ✅ MyFirebaseMessagingService nhận và xử lý notification
- ✅ NotificationHelper quản lý badge count
- ✅ FCMTokenManager đăng ký token với backend
- ✅ Integration với Login/Logout lifecycle
- ✅ AndroidManifest configured với FCM

---

## 📱 Cách hoạt động

1. **App Start**: Lấy FCM token và lưu locally
2. **Login**: Gửi FCM token lên backend để đăng ký
3. **Cart Update**: Backend tự động gửi notification với badge count
4. **Logout**: Deactivate token trên backend và clear badge

---

## 🧪 Test Notification

Sau khi setup xong, bạn có thể test bằng cách:

1. Login vào app
2. Thêm sản phẩm vào giỏ hàng
3. **Đóng app hoàn toàn** (swipe out khỏi recent apps)
4. Notification sẽ xuất hiện với badge count trên app icon

---

## ❗ Troubleshooting

### Lỗi: "google-services.json is missing"
**Giải pháp**: Download file từ Firebase Console và đặt vào đúng vị trí `app/google-services.json`

### Badge không hiển thị
**Nguyên nhân**: Không phải tất cả launcher Android đều hỗ trợ badge
**Giải pháp**: Test trên các launcher phổ biến: Samsung One UI, Google Pixel Launcher, Nova Launcher

### Token không gửi lên backend
**Kiểm tra**:
1. User đã login chưa?
2. Backend API có chạy không? (check `http://10.0.2.2:8080`)
3. Check Logcat với filter: `FCMTokenManager`

---

## 📝 Notes

- File `google-services.json` **không được commit lên Git** (đã thêm vào .gitignore)
- Mỗi developer cần có file riêng của mình
- Backend cần file `firebase-adminsdk.json` để gửi notification

