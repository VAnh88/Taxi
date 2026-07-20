package vn.taxi.trip.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignDriverRequest(
        @NotBlank String driverId
) {
}
