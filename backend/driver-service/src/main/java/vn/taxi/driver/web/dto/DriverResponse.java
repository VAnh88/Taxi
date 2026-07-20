package vn.taxi.driver.web.dto;

import vn.taxi.driver.domain.Driver;

import java.util.UUID;

public record DriverResponse(
        UUID id,
        UUID userId,
        String fullName,
        String phone,
        String status,
        String verificationStatus,
        String shiftStatus,
        Double rating,
        String teamName,
        Double currentLat,
        Double currentLng
) {
    public static DriverResponse from(Driver d) {
        return new DriverResponse(
                d.getId(), d.getUserId(), d.getFullName(), d.getPhone(),
                d.getStatus().name(), d.getVerificationStatus().name(), d.getShiftStatus().name(), d.getRating(),
                d.getTeam() != null ? d.getTeam().getName() : null,
                d.getCurrentLat(), d.getCurrentLng()
        );
    }
}
