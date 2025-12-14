package org.example.carshering.identity.domain.model;

import lombok.Getter;
import org.example.carshering.fleet.domain.valueobject.DateOfIssue;
import org.example.carshering.fleet.domain.valueobject.IssuingAuthority;
import org.example.carshering.identity.domain.valueobject.document.DocumentId;
import org.example.carshering.identity.domain.valueobject.document.DocumentNumber;
import org.example.carshering.identity.domain.valueobject.document.DocumentSeries;
import org.example.carshering.identity.domain.valueobject.document.DocumentTypeId;

import java.time.LocalDate;

@Getter
public class Document {
    // final fields
    private final DocumentId documentId;

    // личные данные
    private final DocumentTypeId documentType;
    private final DocumentSeries documentSeries;
    private final DocumentNumber documentNumber;
    private final DateOfIssue dateOfIssue;
    private final IssuingAuthority issuingAuthority;

    // состояние документа
    private boolean deleted = false;
    private boolean verified = false;

    private Document(DocumentId documentId, DocumentTypeId documentType,
                     DocumentSeries documentSeries,
                     DocumentNumber documentNumber,
                     DateOfIssue dateOfIssue,
                     IssuingAuthority issuingAuthority, boolean verified, boolean deleted) {
        this.documentId = documentId;
        this.documentType = documentType;
        this.documentSeries = documentSeries;
        this.documentNumber = documentNumber;
        this.dateOfIssue = dateOfIssue;
        this.issuingAuthority = issuingAuthority;
        this.verified = verified;
        this.deleted = deleted;
    }

    // создание нового документа
    static Document create(DocumentTypeId documentType,
                           DocumentSeries documentSeries,
                           DocumentNumber documentNumber,
                           DateOfIssue dateOfIssue,
                           IssuingAuthority issuingAuthority) {
        return new Document(null, documentType, documentSeries, documentNumber, dateOfIssue, issuingAuthority, false, false);
    }

    // Восстановление существующего документа из БД (для Repository) // статик + package-private??? только для репозитория
     public static Document restore(DocumentId documentId, DocumentTypeId documentType,
                                   DocumentSeries series, DocumentNumber number,
                                   DateOfIssue dateOfIssue, IssuingAuthority issuingAuthority,
                                   boolean verified, boolean deleted) {
        return new Document(documentId, documentType, series, number, dateOfIssue, issuingAuthority, verified, deleted);
    }

    void markAsDelete() {
        this.deleted = true;
    }

    void verify() {
        this.verified = true;
    }

    public boolean isValid() {
        return !deleted && verified;
    }

    public boolean isExpired(LocalDate currentDate) {
        return false; // Пока нет поля expiryDate
    }

    public boolean hasSameSeriesAndNumber(DocumentSeries series, DocumentNumber number) {
        return this.documentSeries.equals(series) &&
                this.documentNumber.equals(number) && !this.deleted;
    }

}
