package vn.taxi.trip.web.dto;

import vn.taxi.trip.domain.Trip;

import java.time.Instant;
import java.util.UUID;

public record TripResponse(
        UUID id,
        UUID customerId,
        String callerPhone,
        String callerName,
        UUID driverId,
        String pickupAddress,
        Double pickupLat,
        Double pickupLng,
        String dropoffAddress,
        Double dropoffLat,
        Double dropoffLng,
        String status,
        String sourceChannel,
        Long price,
        Double distanceKm,
        UUID cancelReasonId,
        String cancelNote,
        Short customerRating,
        String customerRatingComment,
        Instant requestedAt,
        Instant assignedAt,
        Instant arrivingAt,
        Instant onboardAt,
        Instant completedAt,
        Instant cancelledAt
) {
    public static TripResponse from(Trip t) {
        return new TripResponse(
                t.getId(), t.getCustomerId(), t.getCallerPhone(), t.getCallerName(), t.getDriverId(),
                t.getPickupAddress(), t.getPickupLat(), t.getPickupLng(),
                t.getDropoffAddress(), t.getDropoffLat(), t.getDropoffLng(),
                t.getStatus().name(), t.getSourceChannel().name(), t.getPrice(), t.getDistanceKm(),
                t.getCancelReasonId(), t.getCancelNote(), t.getCustomerRating(), t.getCustomerRatingComment(),
                t.getRequestedAt(), t.getAssignedAt(), t.getArrivingAt(), t.getOnboardAt(), t.getCompletedAt(), t.getCancelledAt()
        );
    }
}
