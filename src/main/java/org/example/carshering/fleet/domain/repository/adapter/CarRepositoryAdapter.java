package org.example.carshering.fleet.domain.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.Money;
import org.example.carshering.common.exceptions.custom.NotFoundException;
import org.example.carshering.fleet.api.dto.responce.MinMaxCellForFilters;
import org.example.carshering.fleet.domain.model.CarDomain;
import org.example.carshering.fleet.domain.repository.CarDomainRepository;
import org.example.carshering.fleet.domain.repository.ImageDomainRepository;
import org.example.carshering.fleet.domain.valueobject.CarStateType;
import org.example.carshering.fleet.domain.valueobject.ImageData;
import org.example.carshering.fleet.domain.valueobject.Year;
import org.example.carshering.fleet.domain.valueobject.name.BodyType;
import org.example.carshering.fleet.domain.valueobject.name.BrandName;
import org.example.carshering.fleet.domain.valueobject.name.CarClassName;
import org.example.carshering.fleet.domain.valueobject.name.ModelName;
import org.example.carshering.fleet.infrastructure.persistence.entity.Car;
import org.example.carshering.fleet.infrastructure.persistence.mapper.CarMapperForDomain;
import org.example.carshering.fleet.infrastructure.persistence.repository.CarRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CarRepositoryAdapter implements CarDomainRepository {

    private final CarRepository carRepository;
    private final ImageDomainRepository imageDomainRepository;
    private final CarMapperForDomain mapper;

    /**
     * Сохранить автомобиль вместе с изображениями
     * Это транзакционная операция - либо сохраняется всё, либо ничего
     *
     * Использует DELETE ALL + INSERT ALL для изображений (простой подход)
     */
    @Override
    @Transactional
    public CarDomain save(CarDomain carDomain) {
        // 1. Сохраняем Car entity
        Car entity = mapper.toEntity(carDomain);
        Car savedEntity = carRepository.save(entity);

        // 2. Получаем ID сохраненной машины
        Long carId = savedEntity.getId();
        CarId savedCarId = new CarId(carId);

        // 3. Заменяем изображения через отдельный репозиторий
        imageDomainRepository.replaceImages(savedCarId, carDomain.getImages());

        // 4. Загружаем актуальные изображения
        List<ImageData> savedImages = imageDomainRepository.findByCarId(savedCarId);

        // 5. Возвращаем доменный объект с актуальными изображениями
        return mapper.toDomain(savedEntity, savedImages);
    }

    /**
     * Массовое сохранение автомобилей
     * ВАЖНО: Изображения НЕ сохраняются при массовом сохранении!
     */
    @Override
    @Transactional
    public List<CarDomain> saveAll(List<CarDomain> cars) {
        return carRepository.saveAll(
                        cars.stream()
                                .map(mapper::toEntity)
                                .toList())
                .stream()
                .map(mapper::toDomain) // Без изображений
                .toList();
    }

    /**
     * Найти автомобиль по ID вместе с изображениями
     */
    @Override
    @Transactional(readOnly = true)
    public CarDomain findById(CarId carId) {
        Car car = carRepository.findById(carId.value()).orElseThrow(
                () -> new NotFoundException("Car not found with id: " + carId.value())
        );


        List<ImageData> images = imageDomainRepository.findByCarId(carId);

        return mapper.toDomain(car, images);
    }

    /**
     * Найти все автомобили без изображений (для производительности)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CarDomain> findAll(Pageable pageable) {
        return carRepository.findAll(pageable)
                .map(mapper::toDomain); // Без изображений
    }

    /**
     * Фильтрация автомобилей без изображений
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CarDomain> findAll(
            Pageable pageable,
            List<BrandName> brands,
            List<ModelName> models,
            Year minYear,
            Year maxYear,
            BodyType bodyType,
            List<CarClassName> carClasses,
            List<CarStateType> carStates,
            LocalDateTime dateStart,
            LocalDateTime dateEnd,
            Money minPrice,
            Money maxPrice
    ) {
        return carRepository.findByFilter(
                brands != null ? brands.stream().map(BrandName::value).toList() : null,
                models != null ? models.stream().map(ModelName::value).toList() : null,
                minYear != null ? minYear.getValue() : null,
                maxYear != null ? maxYear.getValue() : null,
                bodyType != null ? bodyType.value() : null,
                carClasses != null ? carClasses.stream().map(CarClassName::value).toList() : null,
                carStates != null ? carStates.stream().map(CarStateType::name).toList() : null,
                dateStart,
                dateEnd,
                minPrice != null ? minPrice.getAmount() : null,
                maxPrice != null ? maxPrice.getAmount() : null,
                pageable
        ).map(mapper::toDomain); // Без изображений для производительности
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long carId) {
        return carRepository.existsById(carId);
    }

    @Override
    @Transactional(readOnly = true)
    public MinMaxCellForFilters getMinMaxCell(
            List<BrandName> brands,
            List<ModelName> models,
            Year minYear,
            Year maxYear,
            BodyType bodyType,
            List<CarClassName> carClasses,
            List<CarStateType> carStates,
            LocalDateTime dateStart,
            LocalDateTime dateEnd
    ) {
        return carRepository.findMinMaxPriceByFilter(
                brands != null ? brands.stream().map(BrandName::value).toList() : null,
                models != null ? models.stream().map(ModelName::value).toList() : null,
                minYear != null ? minYear.getValue() : null,
                maxYear != null ? maxYear.getValue() : null,
                bodyType != null ? bodyType.value() : null,
                carClasses != null ? carClasses.stream().map(CarClassName::value).toList() : null,
                carStates != null ? carStates.stream().map(CarStateType::name).toList() : null,
                dateStart,
                dateEnd
        );
    }

    @Override
    public CarDomain findByIdAndState(CarId id) {
        return mapper.toDomain( carRepository.findByIdAndState_Active(id.value()));
    }
}
