package vn.taxi.realtime.event;

import java.util.UUID;

public record TripStatusChangedEvent(
        UUID tripId,
        UUID customerId,
        UUID driverId,
        String status
) {
}
