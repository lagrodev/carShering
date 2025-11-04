package org.example.carshering.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
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
