package vn.taxi.auth.web.dto;

import java.util.UUID;

public record MeResponse(
        UUID userId,
        String username,
        String phone,
        String role,
        String status
) {
}
