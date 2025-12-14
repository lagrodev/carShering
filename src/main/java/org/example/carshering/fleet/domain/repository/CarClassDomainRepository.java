package org.example.carshering.fleet.domain.repository;

import org.example.carshering.fleet.domain.model.CarClassDomain;
import org.example.carshering.fleet.domain.valueobject.id.CarClassId;
import org.example.carshering.fleet.domain.valueobject.name.CarClassName;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository для классов автомобилей (Economy, Business, Premium и т.д.)
 * Справочник - кэшируется!
 */
public interface CarClassDomainRepository {

    /**
     * Сохранить класс автомобиля
     */
    CarClassDomain save(CarClassDomain carClass);

    /**
     * Найти класс по ID
     */
    Optional<CarClassDomain> findById(CarClassId carClassId);

    /**
     * Найти класс по названию (case-insensitive)
     */
    Optional<CarClassDomain> findByName(CarClassName carClassName);

    /**
     * Получить все классы автомобилей
     * Результат кэшируется!
     */
    List<CarClassDomain> findAll();

    /**
     * Проверить существование класса
     */
    boolean existsById(CarClassId carClassId);

    /**
     * Проверить существование класса по имени
     */
    boolean existsByName(CarClassName carClassName);
}

