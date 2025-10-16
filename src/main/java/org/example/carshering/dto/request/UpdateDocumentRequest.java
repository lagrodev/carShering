package org.example.carshering.dto.request;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

public record UpdateDocumentRequest(

        Long documentTypeId,

        String series,

        String number,

        @PastOrPresent(message = "Дата выдачи не может быть в будущем")
        LocalDate dateOfIssue,

        String issuingAuthority
) {}
