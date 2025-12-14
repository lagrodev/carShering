package org.example.carshering.fleet.infrastructure.persistence.mapper;

import org.example.carshering.fleet.domain.model.CarModelNameDomain;
import org.example.carshering.fleet.domain.valueobject.id.ModelNameId;
import org.example.carshering.fleet.infrastructure.persistence.entity.Model;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования Model Entity <-> CarModelNameDomain
 */
@Component
public class ModelNameMapper {

    /**
     * Entity -> Domain
     */
    public CarModelNameDomain toDomain(Model entity) {
        if (entity == null) {
            return null;
        }

        return CarModelNameDomain.restore(
                entity.getId() != null ? new ModelNameId(entity.getId()) : null,
                entity.getName()
        );
    }

    /**
     * Domain -> Entity
     */
    public Model toEntity(CarModelNameDomain domain) {
        if (domain == null) {
            return null;
        }

        Model entity = new Model();

        if (domain.getId() != null) {
            entity.setId(domain.getId().value());
        }

        entity.setName(domain.getName());

        return entity;
    }
}

