package vn.taxi.trip.client;

import java.util.UUID;

/** Subset của CustomerResponse bên customer-service. */
public record CustomerDto(
        UUID id,
        UUID userId,
        String type
) {
}
