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
@Table(name = "car_model", schema = "car_rental")
@NoArgsConstructor
@AllArgsConstructor
@Builder
// todo отделить брэнд и модель, а то это тупо, мб кар класс тоже отдельно??? хз пока, но скорее всего, да
public class CarModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    @Column(name = "body_type")
    private String bodyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_class_id")
    private CarClass carClass;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false;


    @OneToMany(mappedBy = "model")
    private List<Car> cars = new ArrayList<>();;

}