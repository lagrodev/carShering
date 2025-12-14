package org.example.carshering.fleet.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.carshering.fleet.domain.valueobject.id.BrandId;
import org.example.carshering.fleet.domain.valueobject.id.CarClassId;
import org.example.carshering.fleet.domain.valueobject.id.ModelNameId;
import org.example.carshering.fleet.domain.valueobject.name.BodyType;

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

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "body_type"))
    })
    private BodyType bodyType;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false;

    // todo поменять
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "car_class_id"))
    })
    private CarClassId carClass;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "model_id"))
    })
    private ModelNameId model;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "brand_id"))
    })
    private BrandId brand;

}