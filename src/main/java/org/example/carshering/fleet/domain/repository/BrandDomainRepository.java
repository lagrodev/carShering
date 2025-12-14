package org.example.carshering.fleet.domain.repository;

import org.example.carshering.fleet.domain.model.CarBrandDomain;
import org.example.carshering.fleet.domain.valueobject.id.BrandId;
import org.example.carshering.fleet.domain.valueobject.name.BrandName;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository для брендов автомобилей
 * Справочник - кэшируется!
 */
public interface BrandDomainRepository {

    /**
     * Сохранить бренд
     */
    CarBrandDomain save(CarBrandDomain brand);

    /**
     * Найти бренд по ID
     */
    Optional<CarBrandDomain> findById(BrandId brandId);

    /**
     * Найти бренд по названию (case-insensitive)
     */
    Optional<CarBrandDomain> findByName(BrandName brandName);

    /**
     * Получить все бренды
     * Результат кэшируется!
     */
    List<CarBrandDomain> findAll();

    /**
     * Проверить существование бренда
     */
    boolean existsById(BrandId brandId);

    /**
     * Проверить существование бренда по имени
     */
    boolean existsByName(BrandName brandName);
}

