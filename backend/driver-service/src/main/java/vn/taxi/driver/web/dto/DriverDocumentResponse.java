package vn.taxi.driver.web.dto;

import vn.taxi.driver.domain.DriverDocument;

import java.time.Instant;
import java.util.UUID;

public record DriverDocumentResponse(
        UUID id,
        UUID driverId,
        String docType,
        String fileUrl,
        String verifyStatus,
        Instant createdAt
) {
    public static DriverDocumentResponse from(DriverDocument d) {
        return new DriverDocumentResponse(d.getId(), d.getDriver().getId(), d.getDocType().name(),
                d.getFileUrl(), d.getVerifyStatus().name(), d.getCreatedAt());
    }
}
