package vn.taxi.driver.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateVerificationRequest(
        @NotBlank String verificationStatus
) {
}
