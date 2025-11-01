package org.example.carshering.dto.request.create;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateDocumentRequest(
        @NotNull Long documentTypeId,
        @NotNull String series,
        @NotNull String number,
        @NotNull LocalDate dateOfIssue,

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
