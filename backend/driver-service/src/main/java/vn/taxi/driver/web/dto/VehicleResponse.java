package vn.taxi.driver.web.dto;

import vn.taxi.driver.domain.Vehicle;

import java.util.UUID;

public record VehicleResponse(
        UUID id,
        UUID driverId,
        String plateNumber,
        String brand,
        String model,
        String vehicleTypeCode,
        String status
) {
    public static VehicleResponse from(Vehicle v) {
        return new VehicleResponse(v.getId(), v.getDriver().getId(), v.getPlateNumber(),
                v.getBrand(), v.getModel(), v.getVehicleType().getCode(), v.getStatus().name());
    }
}
