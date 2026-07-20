package vn.taxi.driver.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDriverDocumentRequest(
        @NotBlank String docType,
        @NotBlank String fileUrl
) {
}
