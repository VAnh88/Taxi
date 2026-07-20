package vn.taxi.trip.client;

import java.util.UUID;

/** Subset của DriverResponse bên driver-service — chỉ lấy field trip-service cần. */
public record AvailableDriverDto(
        UUID id,
        String fullName,
        String phone,
        Double currentLat,
        Double currentLng
) {
}
