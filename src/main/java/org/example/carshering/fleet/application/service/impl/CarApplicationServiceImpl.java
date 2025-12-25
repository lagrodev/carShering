package org.example.carshering.fleet.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.Money;
import org.example.carshering.common.exceptions.custom.NotFoundException;
import org.example.carshering.fleet.api.dto.request.CarFilterRequest;
import org.example.carshering.fleet.api.dto.request.create.CreateCarRequest;
import org.example.carshering.fleet.api.dto.request.update.UpdateCarRequest;
import org.example.carshering.fleet.api.dto.responce.MinMaxCellForFilters;
import org.example.carshering.fleet.application.dto.response.CarDto;
import org.example.carshering.fleet.application.mapper.CarDtoMapper;
import org.example.carshering.fleet.application.service.CarApplicationService;
import org.example.carshering.fleet.domain.model.CarDomain;
import org.example.carshering.fleet.domain.repository.*;
import org.example.carshering.fleet.domain.valueobject.*;
import org.example.carshering.fleet.domain.valueobject.id.ModelId;
import org.example.carshering.fleet.domain.valueobject.name.BodyType;
import org.example.carshering.fleet.domain.valueobject.name.BrandName;
import org.example.carshering.fleet.domain.valueobject.name.CarClassName;
import org.example.carshering.fleet.domain.valueobject.name.ModelName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarApplicationServiceImpl implements CarApplicationService {
    private final CarDomainRepository carRepository;
    private final CarModelDomainRepository carModelRepository;
    private final CarDtoMapper carDtoMapper;


    @Override
    @Transactional
    public CarDto createCar(CreateCarRequest request) {
        // Проверяем, что модель существует
        ModelId modelId = new ModelId(request.modelId());

        if (!carModelRepository.existsById(modelId)) {
            throw new NotFoundException("CarModel with id " + request.modelId() + " not found");
        }

        // Создаем Value Objects
        GosNumber gosNumber = GosNumber.of(request.gosNumber());
        Vin vin = Vin.of(request.vin());
        Year yearOfIssue = Year.of(request.yearOfIssue());
        Money dailyRate = Money.of(request.rent(), "RUB");

        // Определяем начальное состояние (по умолчанию AVAILABLE)
        CarStateType initialState = CarStateType.AVAILABLE;

        // Создаем Domain объект
        CarDomain carDomain = CarDomain.create(
                gosNumber,
                vin,
                dailyRate,
                yearOfIssue,
                modelId,
                initialState
        );

        // Сохраняем
        CarDomain savedCar = carRepository.save(carDomain);

        return carDtoMapper.toDto(savedCar);
    }



    @Override
    public CarDto getCarById(CarId carId) {
        CarDomain car = carRepository.findById(carId);

        return carDtoMapper.toDto(car);
    }

    @Override
    public CarDto getValidCarById(Long carId, boolean favorite) {
        CarId id = new CarId(carId);
        CarDomain car = (carRepository.findByIdAndState(id));

        if (car == null) {
            throw new NotFoundException("Car with id " + carId + " not found");
        }

        // TODO: Обработать параметр favorite (когда будет контекст Favorites)

        return carDtoMapper.toDto(car);
    }

    @Override
    @Transactional
    public CarDto updateCar(Long carId, UpdateCarRequest request) {
        CarId id = new CarId(carId);
        CarDomain car = carRepository.findById(id);

        if (car == null) {
            throw new NotFoundException("Car with id " + carId + " not found");
        }

        // Обновляем поля, если они переданы
        if (request.modelId() != null) {
            ModelId modelId = new ModelId(request.modelId());
            if (!carModelRepository.existsById(modelId)) {
                throw new NotFoundException("CarModel with id " + request.modelId() + " not found");
            }
            car.updateModel(modelId);
        }

        if (request.yearOfIssue() != null) {
            car.updateYearOfIssue(Year.of(request.yearOfIssue()));
        }

        if (request.gosNumber() != null) {
            car.updateGosNumber(GosNumber.of(request.gosNumber()));
        }

        if (request.vin() != null) {
            car.updateVin(Vin.of(request.vin()));
        }

        if (request.rent() != null) {
            car.updateDailyRate(Money.of(BigDecimal.valueOf(request.rent()), "RUB"));
        }

        // Сохраняем изменения
        CarDomain updatedCar = carRepository.save(car);

        return carDtoMapper.toDto(updatedCar);
    }

    @Override
    @Transactional
    public CarDto updateCarState(Long carId, String carStateName) {
        CarId id = new CarId(carId);
        CarDomain car = carRepository.findById(id);

        if (car == null) {
            throw new NotFoundException("Car with id " + carId + " not found");
        }

        // Парсим состояние
        CarStateType newState;
        try {
            newState = CarStateType.valueOf(carStateName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid car state: " + carStateName);
        }

        // Применяем бизнес-логику переходов состояний
        switch (newState) {
            case AVAILABLE -> car.makeAvailable();
            case CONFIRMED -> car.reserve();
            case ACTIVE -> car.startRental();
            case CLOSED -> car.markAsDeleted();
            case CANCELLED -> car.cancelReservation();
            default -> throw new IllegalArgumentException("Unsupported state transition");
        }

        // Сохраняем
        CarDomain updatedCar = carRepository.save(car);

        return carDtoMapper.toDto(updatedCar);
    }

    @Override
    @Transactional
    public void deleteCar(Long carId) {
        CarId id = new CarId(carId);
        CarDomain car = carRepository.findById(id);

        if (car == null) {
            throw new NotFoundException("Car with id " + carId + " not found");
        }

        // Помечаем автомобиль как удаленный (soft delete)
        car.markAsDeleted();

        // Сохраняем изменения
        carRepository.save(car);
    }

    @Override
    public MinMaxCellForFilters getMinMaxCell(CarFilterRequest filter) {
        return carRepository.getMinMaxCell(
                normalizeBrand(filter.brands()),
                normalizeModelNames(filter.models()),
                normalizeYear(filter.minYear()),
                normalizeYear(filter.maxYear()),
                normalizeBodyType(filter.bodyType()),
                normalizeCarClasses(filter.carClasses()),
                normalizeCarState(filter.carState()),
                filter.dateStart(),
                filter.dateEnd()
        );
    }


    @Override
    public Page<CarDto> getAllCars(Pageable pageable, CarFilterRequest filter) {
        // Получаем данные из репозитория
        Page<CarDomain> carPage = carRepository.findAll(
                pageable,
                normalizeBrand(filter.brands()),
                normalizeModelNames(filter.models()),
                normalizeYear(filter.minYear()),
                normalizeYear(filter.maxYear()),
                normalizeBodyType(filter.bodyType()),
                normalizeCarClasses(filter.carClasses()),
                normalizeCarState(filter.carState()),
                filter.dateStart(),
                filter.dateEnd(),
                normalizeMoney(filter.minCell()),
                normalizeMoney(filter.maxCell())
        );

        // Преобразуем в DTO
        return carPage.map(carDtoMapper::toDto);
    }

    private Money normalizeMoney(BigDecimal filter) {
        return filter != null ? Money.of((filter), "RUB") : null;
    }

    private List<ModelName> normalizeModelNames(List<String> filter) {
       return filter != null && !filter.isEmpty()
                ? filter.stream().map(ModelName::new).toList()
                : null;
    }


    private List<BrandName> normalizeBrand(List<String> filter) {
        return filter != null && !filter.isEmpty()
                ? filter.stream().map(BrandName::new).toList()
                : null;
    }

    private Year normalizeYear(Integer year) {
        return year != null ? Year.of(year) : null;
    }

    private BodyType normalizeBodyType(String filter) {
        return filter != null ? new BodyType(filter) : null;
    }

    private  List<CarClassName> normalizeCarClasses(List<String> filter) {

        return filter != null  && !filter.isEmpty() ? filter.stream().map(CarClassName::new).toList()
                : null;
    }

    private  List<CarStateType> normalizeCarState(List<String> filter) {
        return filter != null && !filter.isEmpty()
                ? filter.stream().map(CarStateType::valueOf).toList()
                : null;
    }

    @Override
    public List<String> getAllStates() {
        return Arrays.stream(CarStateType.values()).map(
                Enum::name
        ).toList(
        );
    }
}
