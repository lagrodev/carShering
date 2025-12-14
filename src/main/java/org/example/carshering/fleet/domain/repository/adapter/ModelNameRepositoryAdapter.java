package org.example.carshering.fleet.domain.repository.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.fleet.domain.model.CarModelNameDomain;
import org.example.carshering.fleet.domain.repository.ModelNameDomainRepository;
import org.example.carshering.fleet.domain.valueobject.id.ModelNameId;
import org.example.carshering.fleet.domain.valueobject.name.ModelName;
import org.example.carshering.fleet.infrastructure.persistence.entity.Model;
import org.example.carshering.fleet.infrastructure.persistence.mapper.ModelNameMapper;
import org.example.carshering.fleet.infrastructure.persistence.repository.ModelNameRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Адаптер для работы с названиями моделей автомобилей
 *
 * ВАЖНО: ModelName - это СПРАВОЧНИК, поэтому используется кэширование!
 * - findAll() кэшируется полностью
 * - findByName() кэшируется для быстрого поиска
 * - При save() кэш инвалидируется
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ModelNameRepositoryAdapter implements ModelNameDomainRepository {

    private final ModelNameRepository modelNameRepository;
    private final ModelNameMapper mapper;

    /**
     * Сохранить название модели и сбросить кэш
     */
    @Override
    @Transactional
    @CacheEvict(value = {"modelNames", "modelNameByName"}, allEntries = true)
    public CarModelNameDomain save(CarModelNameDomain modelName) {
        log.debug("Saving model name and evicting cache: {}", modelName.getName());

        Model entity = mapper.toEntity(modelName);
        Model savedEntity = modelNameRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    /**
     * Найти модель по ID (без кэша, т.к. используется редко)
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CarModelNameDomain> findById(ModelNameId modelNameId) {
        return modelNameRepository.findById(modelNameId.value())
                .map(mapper::toDomain);
    }

    /**
     * Найти модель по имени (с кэшем!)
     * Часто используется при создании/обновлении машин
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "modelNameByName", key = "#modelName.value()")
    public Optional<CarModelNameDomain> findByName(ModelName modelName) {
        log.debug("Finding model name by name (will be cached): {}", modelName.value());

        return modelNameRepository.findByNameIgnoreCase(modelName.value())
                .map(mapper::toDomain);
    }

    /**
     * Получить все названия моделей (с кэшем!)
     * Это СПРАВОЧНИК - кэшируется полностью
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "modelNames")
    public List<CarModelNameDomain> findAll() {
        log.debug("Loading all model names (will be cached)");

        return modelNameRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(ModelNameId modelNameId) {
        return modelNameRepository.existsById(modelNameId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(ModelName modelName) {
        return modelNameRepository.findByNameIgnoreCase(modelName.value()).isPresent();
    }
}

