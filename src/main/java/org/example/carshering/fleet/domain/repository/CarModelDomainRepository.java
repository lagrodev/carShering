package org.example.carshering.fleet.domain.repository;

import org.example.carshering.fleet.domain.model.CarModelDomain;
import org.example.carshering.fleet.domain.valueobject.id.BrandId;
import org.example.carshering.fleet.domain.valueobject.id.CarClassId;
import org.example.carshering.fleet.domain.valueobject.id.ModelId;
import org.example.carshering.fleet.domain.valueobject.id.ModelNameId;
import org.example.carshering.fleet.domain.valueobject.name.BodyType;
import org.example.carshering.fleet.domain.valueobject.name.BrandName;
import org.example.carshering.fleet.domain.valueobject.name.CarClassName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository для CarModel
 * Управляет конфигурациями моделей автомобилей
 */
public interface CarModelDomainRepository {

    /**
     * Сохранить модель автомобиля
     */
    CarModelDomain save(CarModelDomain carModel);

    /**
     * Найти модель по ID
     */
    Optional<CarModelDomain> findById(ModelId modelId);

    /**
     * Найти модель по ID (только не удаленные)
     */
    Optional<CarModelDomain> findByIdAndNotDeleted(ModelId modelId);

    /**
     * Найти все модели (с пагинацией)
     */
    Page<CarModelDomain> findAll(Pageable pageable);

    /**
     * Найти модели с фильтрами
     */
    Page<CarModelDomain> findByFilter(
            boolean includeDeleted,
            BrandName brandName,
            BodyType bodyType,
            CarClassName carClassName,
            Pageable pageable
    );

    /**
     * Найти уникальный список типов кузова
     */
    List<String> findDistinctBodyTypes();

    /**
     * Найти конкретную модель по параметрам
     */
    Optional<CarModelDomain> findByParameters(
            BodyType bodyType,
            BrandId brandId,
            CarClassId carClassId,
            ModelNameId modelNameId
    );

    /**
     * Проверить существование модели
     */
    boolean existsById(ModelId modelId);
}
