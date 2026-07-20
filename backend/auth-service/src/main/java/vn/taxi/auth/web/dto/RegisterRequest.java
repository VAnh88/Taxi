package vn.taxi.auth.web.dto;

import jakarta.validation.constraints.NotBlank;
import vn.taxi.auth.domain.UserRole;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String phone,
        @NotBlank String role
) {
    public UserRole roleEnum() {
        return UserRole.valueOf(role.toUpperCase());
    }
}
