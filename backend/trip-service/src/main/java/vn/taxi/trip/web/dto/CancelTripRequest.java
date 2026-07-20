package vn.taxi.trip.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelTripRequest(
        @NotBlank String status,
        @NotBlank String cancelReasonId,
        String cancelNote
) {
}
