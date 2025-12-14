package org.example.carshering.fleet.infrastructure.persistence.mapper;

import org.example.carshering.fleet.domain.model.CarModelDomain;
import org.example.carshering.fleet.domain.valueobject.id.ModelId;
import org.example.carshering.fleet.infrastructure.persistence.entity.CarModel;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования CarModel Entity <-> CarModelDomain
 */
@Component
public class CarModelMapper {

    /**
     * Entity -> Domain
     */
    public CarModelDomain toDomain(CarModel entity) {
        if (entity == null) {
            return null;
        }

        return CarModelDomain.restore(
                entity.getIdModel() != null ? new ModelId(entity.getIdModel()) : null,
                entity.getBodyType(),
                entity.getCarClass(),
                entity.getModel(),
                entity.getBrand(),
                entity.isDeleted()
        );
    }

    /**
     * Domain -> Entity
     */
    public CarModel toEntity(CarModelDomain domain) {
        if (domain == null) {
            return null;
        }

        CarModel entity = new CarModel();

        if (domain.getModelId() != null) {
            entity.setIdModel(domain.getModelId().value());
        }

        entity.setBodyType(domain.getBodyType());
        entity.setCarClass(domain.getCarClass());
        entity.setModel(domain.getModel());
        entity.setBrand(domain.getBrand());
        entity.setDeleted(domain.isDeleted());

        return entity;
    }
}

