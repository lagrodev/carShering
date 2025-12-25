package org.example.carshering.favorites.application.mapper;

import org.example.carshering.favorites.application.dto.FavoriteDto;
import org.example.carshering.favorites.domain.model.FavoriteCar;
import org.example.carshering.favorites.infrastructure.persistence.entity.Favorite;
import org.example.carshering.fleet.application.dto.response.BrandDto;
import org.example.carshering.fleet.domain.model.CarBrandDomain;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FavoriteDtoMapper {
    /**
     * Domain → DTO
     */
    public FavoriteDto toDto(FavoriteCar domain) {
        if (domain == null) {
            return null;
        }

        return new FavoriteDto(
                domain.getFavoriteId() != null ? domain.getFavoriteId().value() : null,
                domain.getCarId().value(),
                domain.getClientId().value(),
                domain.getCreatedAt()
        );
    }

    /**
     * Список Domain → Список DTO
     */
    public List<FavoriteDto> toDtoList(List<FavoriteCar> domains) {
        if (domains == null || domains.isEmpty()) {
            return List.of();
        }

        return domains.stream()
                .map(this::toDto)
                .toList();
    }
}
