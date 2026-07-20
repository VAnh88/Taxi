package vn.taxi.driver.web.dto;

import vn.taxi.driver.domain.VehicleType;

import java.util.UUID;

public record VehicleTypeResponse(
        UUID id,
        String code,
        String nameVi,
        int seatCount
) {
    public static VehicleTypeResponse from(VehicleType v) {
        return new VehicleTypeResponse(v.getId(), v.getCode(), v.getNameVi(), v.getSeatCount());
    }
}
