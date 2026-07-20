package vn.taxi.realtime.event;

import java.util.UUID;

public record DriverLocationChangedEvent(
        UUID driverId,
        Double lat,
        Double lng,
        String shiftStatus,
        String status
) {
}
