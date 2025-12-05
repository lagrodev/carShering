package org.example.carshering.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rental_state", schema = "car_rental")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "state")
    private List<Contract> contracts = new ArrayList<>();
}
