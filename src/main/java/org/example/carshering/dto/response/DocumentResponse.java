package org.example.carshering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@Schema(description = "Document information response")
public record DocumentResponse(
        @Schema(description = "Document ID", example = "1")
        Long id,
        @Schema(description = "Document type", example = "Passport")
        String documentType,
        @Schema(description = "Document series", example = "1234")
        String series,
        @Schema(description = "Document number", example = "567890")
        String number,
        @Schema(description = "Date of issue", example = "2020-01-15")
        LocalDate dateOfIssue,
        @Schema(description = "Issuing authority", example = "МВД России")
        String issuingAuthority,
        @Schema(description = "Verification status", example = "true")
        boolean verified
) {
}
