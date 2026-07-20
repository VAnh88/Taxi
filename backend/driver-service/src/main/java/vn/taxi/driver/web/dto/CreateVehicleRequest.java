package vn.taxi.driver.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateVehicleRequest(
        @NotBlank String plateNumber,
        @NotBlank String brand,
        @NotBlank String model,
        @NotBlank String vehicleTypeCode
) {
}
