package vn.taxi.trip.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTripRequest(
        @NotBlank String pickupAddress,
        @NotNull Double pickupLat,
        @NotNull Double pickupLng,
        @NotBlank String dropoffAddress,
        @NotNull Double dropoffLat,
        @NotNull Double dropoffLng,
        @NotBlank String sourceChannel,
        /** Chỉ dùng khi caller là DISPATCHER/ADMIN tạo cuốc hộ khách đã có tài khoản; nếu null thì lấy từ X-User-Id. */
        String customerId,
        /** Khách vãng lai gọi tổng đài, chưa có tài khoản — bắt buộc khi role DISPATCHER/ADMIN và customerId null. */
        String callerPhone,
        String callerName
) {
}
