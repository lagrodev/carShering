package org.example.carshering.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "car_state", schema = "car_rental")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "state")
    private List<Car> cars = new ArrayList<>();
}
