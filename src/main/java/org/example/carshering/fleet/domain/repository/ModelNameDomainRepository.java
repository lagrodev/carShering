package org.example.carshering.fleet.domain.repository;

import org.example.carshering.fleet.domain.model.CarModelNameDomain;
import org.example.carshering.fleet.domain.valueobject.id.ModelNameId;
import org.example.carshering.fleet.domain.valueobject.name.ModelName;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository для названий моделей автомобилей (Camry, Corolla, Rio и т.д.)
 * Справочник - кэшируется!
 */
public interface ModelNameDomainRepository {

    /**
     * Сохранить название модели
     */
    CarModelNameDomain save(CarModelNameDomain modelName);

    /**
     * Найти модель по ID
     */
    Optional<CarModelNameDomain> findById(ModelNameId modelNameId);

    /**
     * Найти модель по названию (case-insensitive)
     */
    Optional<CarModelNameDomain> findByName(ModelName modelName);

    /**
     * Получить все названия моделей
     * Результат кэшируется!
     */
    List<CarModelNameDomain> findAll();

    /**
     * Проверить существование модели
     */
    boolean existsById(ModelNameId modelNameId);

    /**
     * Проверить существование модели по имени
     */
    boolean existsByName(ModelName modelName);
}

