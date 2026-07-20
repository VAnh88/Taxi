package vn.taxi.trip.domain;

/**
 * Rút gọn từ 14 trạng thái của hệ thống Thiên Đức (xem thien-duc-survey-and-scope.md mục 7.1)
 * xuống bộ tối thiểu cho MVP.
 */
public enum TripStatus {
    REQUESTED,
    DRIVER_ASSIGNED,
    DRIVER_ARRIVING,
    CUSTOMER_ONBOARD,
    COMPLETED,
    CANCELLED_BY_CUSTOMER,
    CANCELLED_BY_DRIVER,
    CANCELLED_BY_DISPATCHER,
    NO_DRIVER_AVAILABLE
}
