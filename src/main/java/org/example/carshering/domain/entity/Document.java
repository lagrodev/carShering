package org.example.carshering.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.carshering.domain.valueobject.DateOfIssue;
import org.example.carshering.domain.valueobject.DocumentNumber;
import org.example.carshering.domain.valueobject.DocumentSeries;
import org.example.carshering.domain.valueobject.IssuingAuthority;

import java.time.LocalDate;

@Entity
@Table(name = "document", schema = "car_rental")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean verified = false;

    @ManyToOne
    @JoinColumn(name = "doctype_id",  nullable = false)
    private DocumentType documentType;

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

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
}
