package vn.taxi.auth.event;

import vn.taxi.auth.domain.UserRole;

import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String username,
        String phone,
        UserRole role
) {
}
