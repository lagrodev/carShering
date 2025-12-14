package org.example.carshering.fleet.infrastructure.persistence.mapper;

import org.example.carshering.fleet.domain.model.CarClassDomain;
import org.example.carshering.fleet.domain.valueobject.id.CarClassId;
import org.example.carshering.fleet.infrastructure.persistence.entity.CarClass;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования CarClass Entity <-> CarClassDomain
 */
@Component
public class CarClassMapper {

    /**
     * Entity -> Domain
     */
    public CarClassDomain toDomain(CarClass entity) {
        if (entity == null) {
            return null;
        }

        return CarClassDomain.restore(
                entity.getId() != null ? new CarClassId(entity.getId()) : null,
                entity.getName()
        );
    }

    /**
     * Domain -> Entity
     */
    public CarClass toEntity(CarClassDomain domain) {
        if (domain == null) {
            return null;
        }

        CarClass entity = new CarClass();

        if (domain.getId() != null) {
            entity.setId(domain.getId().value());
        }

        entity.setName(domain.getName());

        return entity;
    }
}

