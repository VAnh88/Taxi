package vn.taxi.trip.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateTripStatusRequest(
        @NotBlank String status
) {
}
