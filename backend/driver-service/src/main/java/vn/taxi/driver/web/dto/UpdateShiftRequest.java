package vn.taxi.driver.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateShiftRequest(
        @NotBlank String shiftStatus,
        Double lat,
        Double lng
) {
}
