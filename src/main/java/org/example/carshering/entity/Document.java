package org.example.carshering.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "document", schema = "car_rental")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean verified = false;

    @ManyToOne
    @JoinColumn(name = "doctype_id",  nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private String series;

    @Column(nullable = false)
    private String number;

    @Column(name = "date_of_issue",  nullable = false)
    private LocalDate dateOfIssue;

    @Column(name = "issuing_authority", nullable = false)
    private String issuingAuthority;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false;

    @OneToOne
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;
}
