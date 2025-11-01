package org.example.carshering.dto.request.update;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

public record UpdateDocumentRequest(

        Long documentTypeId,

        String series,

        String number,
//todo фильтр по формату??? чтобы я мог так делать: 13.01.2006
        @PastOrPresent(message = "Дата выдачи не может быть в будущем")
        LocalDate dateOfIssue,

        String issuingAuthority
) {}
