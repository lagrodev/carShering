package org.example.carshering.fleet.domain.repository.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.fleet.domain.model.CarClassDomain;
import org.example.carshering.fleet.domain.repository.CarClassDomainRepository;
import org.example.carshering.fleet.domain.valueobject.id.CarClassId;
import org.example.carshering.fleet.domain.valueobject.name.CarClassName;
import org.example.carshering.fleet.infrastructure.persistence.entity.CarClass;
import org.example.carshering.fleet.infrastructure.persistence.mapper.CarClassMapper;
import org.example.carshering.fleet.infrastructure.persistence.repository.CarClassRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Адаптер для работы с классами автомобилей
 *
 * ВАЖНО: CarClass - это СПРАВОЧНИК, поэтому используется кэширование!
 * - findAll() кэшируется полностью
 * - findByName() кэшируется для быстрого поиска
 * - При save() кэш инвалидируется
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class CarClassRepositoryAdapter implements CarClassDomainRepository {

    private final CarClassRepository carClassRepository;
    private final CarClassMapper mapper;

    /**
     * Сохранить класс автомобиля и сбросить кэш
     */
    @Override
    @Transactional
    @CacheEvict(value = {"carClasses", "carClassByName"}, allEntries = true)
    public CarClassDomain save(CarClassDomain carClass) {
        log.debug("Saving car class and evicting cache: {}", carClass.getName());

        CarClass entity = mapper.toEntity(carClass);
        CarClass savedEntity = carClassRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    /**
     * Найти класс по ID (без кэша, т.к. используется редко)
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CarClassDomain> findById(CarClassId carClassId) {
        return carClassRepository.findById(carClassId.value())
                .map(mapper::toDomain);
    }

    /**
     * Найти класс по имени (с кэшем!)
     * Часто используется при создании/обновлении машин
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "carClassByName", key = "#carClassName.value()")
    public Optional<CarClassDomain> findByName(CarClassName carClassName) {
        log.debug("Finding car class by name (will be cached): {}", carClassName.value());

        return carClassRepository.findByNameIgnoreCase(carClassName.value())
                .map(mapper::toDomain);
    }

    /**
     * Получить все классы автомобилей (с кэшем!)
     * Это СПРАВОЧНИК - кэшируется полностью
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "carClasses")
    public List<CarClassDomain> findAll() {
        log.debug("Loading all car classes (will be cached)");

        return carClassRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(CarClassId carClassId) {
        return carClassRepository.existsById(carClassId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(CarClassName carClassName) {
        return carClassRepository.findByNameIgnoreCase(carClassName.value()).isPresent();
    }
}

