package org.example.carshering.fleet.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "car_state", schema = "car_rental")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String status;


}
