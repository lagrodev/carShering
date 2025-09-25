package org.example.carshering.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "car", schema = "car_rental")
@NoArgsConstructor
@AllArgsConstructor
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gos_number", nullable = false, unique = true)
    private String gosNumber;

    @Column(name = "vin", nullable = false, unique = true)
    private String vin;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    private CarModel model;

    @ManyToOne
    @JoinColumn(name = "state_id", nullable = false)
    private CarState state;

    @OneToMany(mappedBy = "car")
    private List<RentalState> contracts = new ArrayList<>();;

}