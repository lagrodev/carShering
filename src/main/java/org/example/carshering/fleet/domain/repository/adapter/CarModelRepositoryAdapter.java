package org.example.carshering.fleet.domain.repository.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.common.exceptions.custom.NotFoundException;
import org.example.carshering.fleet.domain.model.CarModelDomain;
import org.example.carshering.fleet.domain.repository.CarModelDomainRepository;
import org.example.carshering.fleet.domain.valueobject.id.BrandId;
import org.example.carshering.fleet.domain.valueobject.id.CarClassId;
import org.example.carshering.fleet.domain.valueobject.id.ModelId;
import org.example.carshering.fleet.domain.valueobject.id.ModelNameId;
import org.example.carshering.fleet.domain.valueobject.name.BodyType;
import org.example.carshering.fleet.domain.valueobject.name.BrandName;
import org.example.carshering.fleet.domain.valueobject.name.CarClassName;
import org.example.carshering.fleet.infrastructure.persistence.entity.CarModel;
import org.example.carshering.fleet.infrastructure.persistence.mapper.CarModelMapper;
import org.example.carshering.fleet.infrastructure.persistence.repository.CarModelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Адаптер для работы с моделями автомобилей
 *
 * CarModel - это НЕ справочник (создается динамически для каждой комбинации параметров),
 * поэтому НЕ кэшируется
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class CarModelRepositoryAdapter implements CarModelDomainRepository {

    private final CarModelRepository carModelRepository;
    private final CarModelMapper mapper;

    @Override
    @Transactional
    public CarModelDomain save(CarModelDomain carModel) {
        log.debug("Saving car model: {}", carModel.getModelId());

        CarModel entity = mapper.toEntity(carModel);
        CarModel savedEntity = carModelRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CarModelDomain> findById(ModelId modelId) {
        return carModelRepository.findById(modelId.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CarModelDomain> findByIdAndNotDeleted(ModelId modelId) {
        return carModelRepository.findByIdAndDeletedFalse(modelId.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CarModelDomain> findAll(Pageable pageable) {
        return carModelRepository.findAll(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<CarModelDomain> findByFilter(boolean includeDeleted, BrandName brandName, BodyType bodyType, CarClassName carClassName, Pageable pageable) {
        return carModelRepository.findModelsByFilter(
                includeDeleted,
                brandName != null ? brandName.value() : null,
                bodyType != null ? bodyType.value() : null,
                carClassName != null ? carClassName.value() : null,
                pageable
        ).map(mapper::toDomain);
    }



    @Override
    @Transactional(readOnly = true)
    public List<String> findDistinctBodyTypes() {
        return carModelRepository.findDistinctBodyTypes();
    }

    /**
     * Найти ТОЧНУЮ комбинацию параметров
     *
     * РАЗНИЦА С findByFilter:
     * - findByParameters - ищет ТОЧНОЕ совпадение ВСЕХ параметров
     * - findByFilter - ищет по частичному совпадению с пагинацией
     *
     * Используется при создании автомобиля, чтобы проверить:
     * "Есть ли уже CarModel с такой комбинацией Brand+Model+BodyType+Class?"
     *
     * Пример:
     * findByParameters(
     *   bodyType = SEDAN,
     *   brandId = 1 (BMW),
     *   carClassId = 2 (Business),
     *   modelNameId = 3 (3-Series)
     * )
     * → Вернет CarModel, если такая ТОЧНАЯ комбинация уже существует
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CarModelDomain> findByParameters(
            BodyType bodyType,
            BrandId brandId,
            CarClassId carClassId,
            ModelNameId modelNameId
    ) {
        log.debug("Finding CarModel by exact parameters: bodyType={}, brandId={}, carClassId={}, modelNameId={}",
                bodyType, brandId, carClassId, modelNameId);

        // Преобразуем Value Objects в примитивы для JPA запроса
        String bodyTypeValue = bodyType != null ? bodyType.value() : null;
        Long brandIdValue = brandId != null ? brandId.value() : null;
        Long carClassIdValue = carClassId != null ? carClassId.value() : null;
        Long modelNameIdValue = modelNameId != null ? modelNameId.value() : null;

        // Используем метод из CarModelRepository
        // ВАЖНО: Этот метод ищет ТОЧНОЕ совпадение ВСЕХ параметров
        return carModelRepository.findByBodyTypeAndBrand_IdAndCarClass_IdAndModel_Id(
                bodyTypeValue,
                brandIdValue,
                carClassIdValue,
                modelNameIdValue
        ).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(ModelId modelId) {
        return carModelRepository.existsById(modelId.value());
    }
}

