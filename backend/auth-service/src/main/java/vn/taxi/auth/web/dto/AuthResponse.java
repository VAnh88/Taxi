package vn.taxi.auth.web.dto;

import java.util.UUID;

public record AuthResponse(
        String token,
        UUID userId,
        String username,
        String role
) {
}
