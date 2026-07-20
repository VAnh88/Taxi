package vn.taxi.customer.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCustomerTypeRequest(
        @NotBlank String type,
        /** Bắt buộc khi type = BLACKLIST. */
        String blacklistReason
) {
}
