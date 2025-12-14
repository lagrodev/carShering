package org.example.carshering.identity.application.dto.response;

public record DocumentDto(
        Long id,
        Long documentTypeId,
        String documentType,
        String documentSeries,
        String documentNumber,
        String dateOfIssue,
        String issuingAuthority,
        boolean verified,
        boolean deleted
) {
}
