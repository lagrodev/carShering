package org.example.carshering.favorites.infrastructure.persistence.mapper;

import org.example.carshering.favorites.domain.model.FavoriteCar;
import org.example.carshering.favorites.domain.valueobject.FavoriteId;
import org.example.carshering.favorites.infrastructure.persistence.entity.Favorite;
import org.example.carshering.fleet.domain.model.CarBrandDomain;
import org.example.carshering.fleet.domain.valueobject.id.BrandId;
import org.example.carshering.fleet.infrastructure.persistence.entity.Brand;
import org.springframework.stereotype.Component;

@Component
public class FavoriteMapper {
    /**
     * Entity -> Domain
     */
    public FavoriteCar toDomain(Favorite entity) {
        if (entity == null) {
            return null;
        }

        return FavoriteCar.reconstruct(
                entity.getId() != null ? new FavoriteId(entity.getId()) : null,
                entity.getCar(),
                entity.getClient(),
                entity.getCreatedAt()
        );
    }

    /**
     * Domain -> Entity
     */
    public Favorite toEntity(FavoriteCar domain) {
        if (domain == null) {
            return null;
        }

        Favorite entity = new Favorite();

        if (domain.getFavoriteId() != null) {
            entity.setId(domain.getFavoriteId().value());
        }

        entity.setCar(domain.getCarId());
        entity.setClient(domain.getClientId());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
