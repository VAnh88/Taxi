// Dùng chung giữa admin-web, mobile-customer, mobile-driver.
// Đối chiếu với DTO backend: driver-service/web/dto, trip-service/web/dto, customer-service/web/dto.

export type UserRole = 'ADMIN' | 'DISPATCHER' | 'DRIVER' | 'CUSTOMER';

export interface AuthResponse {
  token: string;
  userId: string;
  username: string;
  role: UserRole;
}

export type DriverStatus = 'ACTIVE' | 'LOCKED' | 'INACTIVE';
export type ShiftStatus = 'ON' | 'OFF';
/** Cổng duyệt hồ sơ — chỉ VERIFIED mới lên ca/nhận cuốc được. */
export type VerificationStatus = 'PENDING' | 'VERIFIED' | 'REJECTED';

export interface Driver {
  id: string;
  userId: string;
  fullName: string;
  phone: string;
  status: DriverStatus;
  verificationStatus: VerificationStatus;
  shiftStatus: ShiftStatus;
  rating: number;
  teamName: string | null;
  currentLat: number | null;
  currentLng: number | null;
}

export interface VehicleType {
  id: string;
  code: string;
  nameVi: string;
  seatCount: number;
}

export type DocumentType = 'CCCD' | 'DRIVER_LICENSE' | 'VEHICLE_REGISTRATION' | 'INSURANCE';

export interface DriverDocument {
  id: string;
  driverId: string;
  docType: DocumentType;
  fileUrl: string;
  verifyStatus: VerificationStatus;
  createdAt: string;
}

export type CustomerType = 'VIP' | 'APP' | 'REGULAR' | 'BLACKLIST';

export interface Customer {
  id: string;
  userId: string;
  fullName: string;
  phone: string;
  type: CustomerType;
  blacklistReason: string | null;
}

export type CancelReasonActor = 'CUSTOMER' | 'DRIVER' | 'DISPATCHER' | 'ALL';

export interface CancelReason {
  id: string;
  nameVi: string;
  nameEn: string | null;
  appliesTo: CancelReasonActor;
}

// Rút gọn từ 14 trạng thái Thiên Đức — xem thien-duc-survey-and-scope.md mục 7.1.
export type TripStatus =
  | 'REQUESTED'
  | 'DRIVER_ASSIGNED'
  | 'DRIVER_ARRIVING'
  | 'CUSTOMER_ONBOARD'
  | 'COMPLETED'
  | 'CANCELLED_BY_CUSTOMER'
  | 'CANCELLED_BY_DRIVER'
  | 'CANCELLED_BY_DISPATCHER'
  | 'NO_DRIVER_AVAILABLE';

export type SourceChannel = 'CUSTOMER_APP' | 'DISPATCHER' | 'WALK_IN';

export interface Trip {
  id: string;
  /** Null khi khách vãng lai gọi tổng đài chưa có tài khoản — dùng callerPhone/callerName. */
  customerId: string | null;
  callerPhone: string | null;
  callerName: string | null;
  driverId: string | null;
  pickupAddress: string;
  pickupLat: number;
  pickupLng: number;
  dropoffAddress: string;
  dropoffLat: number;
  dropoffLng: number;
  status: TripStatus;
  sourceChannel: SourceChannel;
  price: number | null;
  distanceKm: number | null;
  cancelReasonId: string | null;
  cancelNote: string | null;
  customerRating: number | null;
  customerRatingComment: string | null;
  requestedAt: string;
  assignedAt: string | null;
  arrivingAt: string | null;
  onboardAt: string | null;
  completedAt: string | null;
  cancelledAt: string | null;
}

export interface CreateTripRequest {
  pickupAddress: string;
  pickupLat: number;
  pickupLng: number;
  dropoffAddress: string;
  dropoffLat: number;
  dropoffLng: number;
  sourceChannel: SourceChannel;
  /** DISPATCHER/ADMIN tạo hộ khách đã có tài khoản. */
  customerId?: string;
  /** DISPATCHER/ADMIN tạo hộ khách vãng lai gọi điện, chưa có tài khoản (bắt buộc nếu không có customerId). */
  callerPhone?: string;
  callerName?: string;
}

export interface AssignDriverRequest {
  driverId: string;
}

export interface CancelTripRequest {
  status: TripStatus;
  cancelReasonId: string;
  cancelNote?: string;
}

export interface RateTripRequest {
  rating: number;
  comment?: string;
}

export interface TripStatusChangedEvent {
  tripId: string;
  customerId: string | null;
  driverId: string | null;
  status: TripStatus;
}

/** Đẩy qua STOMP /topic/fleet/locations — dispatcher-web dùng để cập nhật marker xe trên bản đồ realtime. */
export interface FleetLocationEvent {
  driverId: string;
  lat: number | null;
  lng: number | null;
  shiftStatus: ShiftStatus;
  status: DriverStatus;
}

export const TRIP_STATUS_LABEL_VI: Record<TripStatus, string> = {
  REQUESTED: 'Đang tìm tài xế...',
  DRIVER_ASSIGNED: 'Đã tìm thấy tài xế, đang chờ tài xế xác nhận',
  DRIVER_ARRIVING: 'Tài xế đang tới điểm đón',
  CUSTOMER_ONBOARD: 'Đang di chuyển',
  COMPLETED: 'Chuyến đi hoàn tất',
  NO_DRIVER_AVAILABLE: 'Không tìm thấy tài xế gần bạn',
  CANCELLED_BY_CUSTOMER: 'Bạn đã hủy chuyến',
  CANCELLED_BY_DRIVER: 'Tài xế đã hủy chuyến',
  CANCELLED_BY_DISPATCHER: 'Tổng đài đã hủy chuyến',
};
