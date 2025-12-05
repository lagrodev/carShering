package org.example.carshering.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@Table(name = "car_classes", schema = "car_rental")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // например: "Economy", "Premium", "SUV"

    @OneToMany(mappedBy = "carClass")
    private List<CarModel> carSpecifications = new ArrayList<>();
}
