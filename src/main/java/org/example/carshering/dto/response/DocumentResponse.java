package org.example.carshering.dto.response;

import java.time.LocalDate;

public record DocumentResponse(
        Long id,

        String documentType,
        String series,
        String number,
        LocalDate dateOfIssue,
        String issuingAuthority,
        boolean verified
) {
}
