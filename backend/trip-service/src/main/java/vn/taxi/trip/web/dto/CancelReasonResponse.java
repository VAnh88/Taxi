package vn.taxi.trip.web.dto;

import vn.taxi.trip.domain.CancelReason;

import java.util.UUID;

public record CancelReasonResponse(
        UUID id,
        String nameVi,
        String nameEn,
        String appliesTo
) {
    public static CancelReasonResponse from(CancelReason r) {
        return new CancelReasonResponse(r.getId(), r.getNameVi(), r.getNameEn(), r.getAppliesTo().name());
    }
}
