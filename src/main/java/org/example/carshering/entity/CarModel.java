package org.example.carshering.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "car_model", schema = "car_rental")
@NoArgsConstructor
@AllArgsConstructor
public class CarModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idModel;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(name = "body_type")
    private String bodyType;

    @Column(name = "car_class")
    private String carClass;



    @OneToMany(mappedBy = "model")
    private List<Car> cars = new ArrayList<>();;


}