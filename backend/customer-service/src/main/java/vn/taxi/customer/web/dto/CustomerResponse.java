package vn.taxi.customer.web.dto;

import vn.taxi.customer.domain.Customer;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        UUID userId,
        String fullName,
        String phone,
        String type,
        String blacklistReason
) {
    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(c.getId(), c.getUserId(), c.getFullName(), c.getPhone(),
                c.getType().name(), c.getBlacklistReason());
    }
}
