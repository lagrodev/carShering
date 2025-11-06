package org.example.carshering.dto.request.update;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@Schema(description = "Request to update document information")
public record UpdateDocumentRequest(
        @Schema(description = "ID of the document type", example = "1")
        Long documentTypeId,
        @Schema(description = "Document series", example = "1234")
        String series,
        @Schema(description = "Document number", example = "567890")
        String number,
//todo фильтр по формату??? чтобы я мог так делать: 13.01.2006
        @Schema(description = "Date of issue (cannot be in the future)", example = "2020-01-15")
        @PastOrPresent(message = "Дата выдачи не может быть в будущем")
        LocalDate dateOfIssue,
        @Schema(description = "Issuing authority", example = "МВД России")
        String issuingAuthority
) {}
