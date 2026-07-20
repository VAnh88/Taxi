# Hệ thống taxi mới (thay thế Thiên Đức Admin)

Xem [thien-duc-survey-and-scope.md](thien-duc-survey-and-scope.md) cho bối cảnh nghiệp vụ/scope, [business-analysis-and-db-design.md](business-analysis-and-db-design.md) cho thiết kế DB đã chốt, và plan kiến trúc chi tiết trong phiên làm việc (Phase 1: core tối thiểu chạy end-to-end).

**Quyết định nghiệp vụ đã chốt (áp dụng trong code):** đánh giá tài xế 1 chiều sau khi hoàn tất chuyến; tài xế bắt buộc được admin duyệt hồ sơ (`verificationStatus=VERIFIED`) mới lên ca/nhận cuốc được; hủy chuyến không giới hạn số lần nhưng luôn phải chọn lý do trong `/api/cancel-reasons`; 1 tài xế cố định 1 xe.

## Cấu trúc

- `backend/` — Maven multi-module (Java 17, Spring Boot 3, Spring Cloud): discovery-server, config-server, api-gateway, auth-service, driver-service, customer-service, trip-service, realtime-service.
- `frontend/` — **Nx monorepo** (dựng bằng Angular CLI/Expo CLI thật qua Nx generator, không viết tay):
  - `apps/admin-web` — Angular 22 (Login, Driver list, Trip list), build bằng `@nx/angular`.
  - `apps/mobile-customer` — React Native/Expo (đặt xe, theo dõi chuyến realtime), build bằng `@nx/expo`.
  - `apps/mobile-driver` — React Native/Expo (lên ca, nhận cuốc, cập nhật trạng thái), build bằng `@nx/expo`.
  - `apps/dispatcher-web` — Angular 22, màn hình tổng đài ("Operator") — bản đồ Google Maps theo dõi vị trí xe realtime, tạo lệnh điều xe cho khách gọi điện (không cần tài khoản), gán tay tài xế, bảng theo dõi cuốc.
  - `libs/shared-types` — TypeScript DTO/enum dùng chung cả 4 app (`Trip`, `Driver`, `Customer`, `TripStatus`, `FleetLocationEvent`...), import qua `@org/shared-types`. Sửa 1 chỗ, cả 4 app tự động dùng type mới, tránh lệch API contract.
- `infra/` — docker-compose + cấu hình Postgres/Config Server.

## Chạy hạ tầng + backend + admin-web

```bash
cd infra
docker-compose up --build
```

Kiểm tra:
- Eureka dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- Admin web: http://localhost:4200
- Dispatcher web: http://localhost:4300 (cần cấu hình Google Maps API key trước, xem bên dưới)
- Postgres từ máy host: cổng **5433** (máy dev có Postgres khác chiếm 5432; trong docker network các service vẫn nối `postgres:5432`). Schema đầy đủ 7 database / 26 bảng tự tạo từ `infra/postgres-init/` (01→08) khi volume mới — thiết kế chi tiết xem [db-design-complete.md](db-design-complete.md).

## Luồng end-to-end thử qua Postman (qua gateway :8080)

1. `POST /api/auth/register` — tạo tài khoản CUSTOMER: `{"username":"kh1","password":"123456","phone":"0900000001","role":"CUSTOMER"}`
2. `POST /api/auth/register` — tạo tài khoản DRIVER: `{"username":"tx1","password":"123456","phone":"0900000002","role":"DRIVER"}`
3. `POST /api/auth/login` với tài khoản DRIVER → lấy token, gọi `GET /api/drivers/me` (kèm `Authorization: Bearer <token>`) để lấy `driverId`.
4. `PATCH /api/drivers/{driverId}/shift` — `{"shiftStatus":"ON","lat":21.3187,"lng":105.5908}` để tài xế sẵn sàng nhận cuốc.
5. `POST /api/auth/login` với tài khoản CUSTOMER → lấy token.
6. `POST /api/trips` (kèm token khách hàng) — `{"pickupAddress":"A","pickupLat":21.319,"pickupLng":105.591,"dropoffAddress":"B","dropoffLat":21.33,"dropoffLng":105.60,"sourceChannel":"CUSTOMER_APP"}` → hệ thống tự gán tài xế gần nhất, trạng thái `DRIVER_ASSIGNED`.
7. `PATCH /api/trips/{tripId}/status` lần lượt `{"status":"DRIVER_ARRIVING"}` → `{"status":"CUSTOMER_ONBOARD"}` → `{"status":"COMPLETED"}`.
8. Mở admin-web (`localhost:4200`), đăng nhập, xem danh sách tài xế/chuyến đi cập nhật.

## Luồng tổng đài qua dispatcher-web

1. Tạo tài khoản DISPATCHER: `POST /api/auth/register` — `{"username":"dv1","password":"123456","phone":"0900000003","role":"DISPATCHER"}`.
2. Cho ít nhất 1 tài xế lên ca (bước 3-4 ở luồng Postman phía trên) để có xe hiện trên bản đồ.
3. Mở http://localhost:4300, đăng nhập bằng tài khoản DISPATCHER.
4. Nhập SĐT + họ tên khách gọi điện (không cần khách có tài khoản) + địa chỉ đón/trả → "Lệnh Điều Xe (F4)" → hệ thống tự gán tài xế gần nhất, hoặc chọn 1 dòng trong bảng "Cuốc đang chờ" rồi bấm vào 1 xe trên bản đồ để gán tay/ghi đè.
5. Marker xe trên bản đồ tự cập nhật vị trí realtime mỗi khi tài xế PATCH `/api/drivers/{id}/shift`.

### Cấu hình Google Maps API key (bắt buộc để xem bản đồ)
Tự tạo API key ở [Google Cloud Console](https://console.cloud.google.com/google/maps-apis) (bật **Maps JavaScript API**, giới hạn theo domain/IP dùng thực tế), sau đó điền vào `frontend/apps/dispatcher-web/src/app/core/api.config.ts` (biến `GOOGLE_MAPS_API_KEY`). Claude không tự tạo/nhập credential này. Chưa có key thì các phần còn lại của dispatcher-web (form, bảng theo dõi) vẫn chạy được, chỉ riêng bản đồ báo lỗi.

## Làm việc với Nx workspace (frontend/)

```bash
cd frontend
npm install                       # cài 1 lần cho cả 3 app + lib (npm workspaces)

npx nx serve admin-web            # chạy admin-web dev server (localhost:4200)
npx nx serve dispatcher-web       # chạy dispatcher-web dev server (localhost:4300)
npx nx start mobile-customer      # chạy Expo dev server cho app khách hàng
npx nx start mobile-driver        # chạy Expo dev server cho app tài xế

npx nx build admin-web            # build production admin-web
npx nx export mobile-customer     # bundle thử mobile-customer (web/android/ios) không cần thiết bị

npx nx sync                       # đồng bộ lại TS project references sau khi thêm lib/app mới
```

Android emulator dùng sẵn `10.0.2.2` để trỏ về máy host (xem `apps/mobile-*/src/api/config.ts`). Thiết bị thật cần đổi thành IP LAN của máy chạy docker-compose.

**Lưu ý kỹ thuật:** `frontend/` dùng preset Nx "empty-template" (TypeScript project references + `composite`/`emitDeclarationOnly`), vốn Angular chưa hỗ trợ đầy đủ (xem cảnh báo khi chạy `nx add @nx/angular`). Đã vá bằng cách tắt `composite`/`declaration`/`emitDeclarationOnly` riêng trong `tsconfig.app.json` của từng app — `nx build`/`nx export` chạy tốt, nhưng `nx typecheck` (chạy qua `tsc --build`) sẽ báo lỗi cấu hình xung đột — đây là giới hạn đã biết của tổ hợp Nx + Angular/Expo project-references, không phải lỗi code. Dùng `nx build`/`nx export` làm nguồn xác nhận chính thay vì `nx typecheck`.

## Ngoài phạm vi Phase 1

Pricing service riêng, Agency/Commission, Payroll, đầy đủ 12 kênh nguồn đơn + 14 trạng thái như hệ thống cũ, CI/CD, production hardening, map picker thật cho mobile app (hiện nhập tay lat/lng), GPS thật cho tài xế (hiện nhập tay vị trí). Xem phần "Việc ngoài phạm vi Phase 1" trong plan đã duyệt.
