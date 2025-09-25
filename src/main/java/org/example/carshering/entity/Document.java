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

    @Column(name = "document_type")
    private String documentType;

    private String series;

    private String number;

    @Column(name = "date_of_issue")
    private LocalDate dateOfIssue;

    @Column(name = "issuing_authority")
    private String issuingAuthority;

    @OneToOne
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;
}
