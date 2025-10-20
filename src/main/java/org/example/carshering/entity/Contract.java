package org.example.carshering.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "contract", schema = "car_rental")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_start",  nullable = false)
    private LocalDate dataStart;

    @Column(name = "data_end",   nullable = false)
    private LocalDate dataEnd;

    @Column(name = "total_cost", nullable = false)
    private Double totalCost;

    private String comment;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;


// todo комментарии

    @ManyToOne
    @JoinColumn(name = "state_id", nullable = false)
    private RentalState state;
}
