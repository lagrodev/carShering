package org.example.carshering.dto.request.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@Schema(description = "Request to create a new document")
public record CreateDocumentRequest(
        @Schema(description = "ID of the document type", example = "1")
        @NotNull Long documentTypeId,
        @Schema(description = "Document series", example = "1234")
        @NotNull String series,
        @Schema(description = "Document number", example = "567890")
        @NotNull String number,
        @Schema(description = "Date of issue", example = "2020-01-15")
        @NotNull LocalDate dateOfIssue,
        @Schema(description = "Issuing authority", example = "МВД России")
        @NotNull String issuingAuthority
) {

    /*{
        "documentTypeId": 1,
            "series": "123123",
            "number": "123",
            "dateOfIssue": 2018,
            "issuingAuthority": "РФ, ген прокуротура 228 339"

    }*/
}
