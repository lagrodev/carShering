package org.example.carshering.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "car", schema = "car_rental")
@NoArgsConstructor
@AllArgsConstructor
@Builder
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


    private Double rent;

    @Column(name = "year_of_issue")
    private Integer yearOfIssue;

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    private CarModel model;

    @ManyToOne
    @JoinColumn(name = "state_id", nullable = false)
    private CarState state;

    @OneToMany(mappedBy = "car")
    private List<Contract> contracts = new ArrayList<>();;

    @OneToMany(mappedBy = "car")
    private List<Favorite> favorites = new ArrayList<>();


    // TODO: @CreateDate @UpdateDate
}