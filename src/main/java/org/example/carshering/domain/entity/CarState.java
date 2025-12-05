package org.example.carshering.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "state")
    private List<Car> cars = new ArrayList<>();
}
