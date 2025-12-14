package org.example.carshering.identity.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.carshering.fleet.domain.valueobject.DateOfIssue;
import org.example.carshering.fleet.domain.valueobject.IssuingAuthority;
import org.example.carshering.identity.domain.valueobject.document.DocumentNumber;
import org.example.carshering.identity.domain.valueobject.document.DocumentSeries;
import org.example.carshering.identity.domain.valueobject.document.DocumentTypeId;

@Entity
@Table(name = "document", schema = "car_rental")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean verified = false;



    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "series", nullable = false))})
    private DocumentSeries series;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "number", nullable = false))})
    private DocumentNumber number;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "date_of_issue", nullable = false))})
    private DateOfIssue dateOfIssue;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "issuing_authority", nullable = false))})
    private IssuingAuthority issuingAuthority;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false;


    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "doctype_id", nullable = false))})
    private DocumentTypeId documentType;


}
