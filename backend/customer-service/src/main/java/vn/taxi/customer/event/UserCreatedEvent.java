package vn.taxi.customer.event;

import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String username,
        String phone,
        String role
) {
}
