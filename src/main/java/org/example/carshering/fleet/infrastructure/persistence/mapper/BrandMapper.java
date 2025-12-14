package org.example.carshering.fleet.infrastructure.persistence.mapper;

import org.example.carshering.fleet.domain.model.CarBrandDomain;
import org.example.carshering.fleet.domain.valueobject.id.BrandId;
import org.example.carshering.fleet.infrastructure.persistence.entity.Brand;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования Brand Entity <-> CarBrandDomain
 */
@Component
public class BrandMapper {

    /**
     * Entity -> Domain
     */
    public CarBrandDomain toDomain(Brand entity) {
        if (entity == null) {
            return null;
        }

        return CarBrandDomain.restore(
                entity.getId() != null ? new BrandId(entity.getId()) : null,
                entity.getName()
        );
    }

    /**
     * Domain -> Entity
     */
    public Brand toEntity(CarBrandDomain domain) {
        if (domain == null) {
            return null;
        }

        Brand entity = new Brand();

        if (domain.getId() != null) {
            entity.setId(domain.getId().value());
        }

        entity.setName(domain.getName());

        return entity;
    }
}

