package org.example.carshering.fleet.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.carshering.fleet.domain.valueobject.name.CarClassName;


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

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "name", nullable = false))
    })
    private CarClassName name; // например: "Economy", "Premium", "SUV"


}
