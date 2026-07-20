package vn.taxi.driver.event;

import java.util.UUID;

public record DriverLocationChangedEvent(
        UUID driverId,
        Double lat,
        Double lng,
        String shiftStatus,
        String status
) {
}
