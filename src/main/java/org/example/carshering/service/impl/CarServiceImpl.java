package org.example.carshering.service.impl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.CarFilterRequest;
import org.example.carshering.dto.request.CreateCarRequest;
import org.example.carshering.dto.request.UpdateCarRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.entity.Car;
import org.example.carshering.entity.CarModel;
import org.example.carshering.entity.CarState;
import org.example.carshering.mapper.CarMapper;
import org.example.carshering.repository.CarModelRepository;
import org.example.carshering.repository.CarRepository;
import org.example.carshering.repository.CarStateRepository;
import org.example.carshering.service.CarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final CarModelRepository carModelRepository;

    private final CarStateRepository stateRepository;

    @Override
    public CarDetailResponse findCar(Long carId) {
        Car car = this.carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));
        return carMapper.toDetailDto(car);

    }



    @Override
    public Car getEntity(Long carId) {
        return carRepository.findById(carId)
                .orElseThrow(() -> new ValidationException("Автомобиль не найден"));
    }


    @Override
    public CarDetailResponse findValidCar(Long carId) {
        Car car = this.carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        if (!car.getState().getStatus().equals("AVAILABLE")) {
            throw new ValidationException("Автомобиль не найден");
        }

        return carMapper.toDetailDto(car);

    }

    @Override
    public Page<CarListItemResponse> getAllCars(Pageable pageable, CarFilterRequest filter) {
        validateSortProperties(pageable.getSort());

        List<String> brands = isEmpty(filter.brands()) ? null : filter.brands();
        List<String> models = isEmpty(filter.models()) ? null : filter.models();
        List<String> carClasses = isEmpty(filter.carClasses()) ? null : filter.carClasses();

        return carRepository.findByFilter(
                false,
                brands,
                models,
                filter.minYear(),
                filter.maxYear(),
                filter.bodyType(),
                carClasses,
                pageable
        ).map(carMapper::toListItemDto);
    }

    @Override
    public Page<CarListItemResponse> getAllValidCars(Pageable pageable, CarFilterRequest filter) {
        validateSortProperties(pageable.getSort());

        List<String> brands = isEmpty(filter.brands()) ? null : filter.brands();
        List<String> models = isEmpty(filter.models()) ? null : filter.models();
        List<String> carClasses = isEmpty(filter.carClasses()) ? null : filter.carClasses();

        return carRepository.findByFilter(
                true,
                brands,
                models,
                filter.minYear(),
                filter.maxYear(),
                filter.bodyType(),
                carClasses,
                pageable
        ).map(carMapper::toListItemDto);
    }

    private boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }


    private void validateSortProperties(Sort sort) {
        for (Sort.Order order : sort) {
            if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
                throw new IllegalArgumentException("Недопустимое поле сортировки: " + order.getProperty());
            }
        }
    }
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id", "gosNumber", "yearOfIssue", "rent",
            "model.brand", "model.model", "model.bodyType", "model.carClass"
    );



    @Override
    public CarDetailResponse createCar(CreateCarRequest request) {
        // todo
        CarModel model = carModelRepository.findById(request.modelId())
                .orElseThrow(() -> new RuntimeException("Car model not found"));

        if (carRepository.existsByGosNumber(request.gosNumber())) {
            throw new RuntimeException("Gos number already exists");
        }
        if (carRepository.existsByVin(request.vin())) {
            throw new RuntimeException("VIN already exists");
        }

        Car car = carMapper.toEntity(request, model);

        CarState state = stateRepository.findByStatus("AVAILABLE")
                .orElseThrow(() -> new RuntimeException("Car state not found"));

        car.setState(state);

        return carMapper.toDetailDto(carRepository.save(car));
    }



    @Override
    public CarDetailResponse updateCar(Long carId, UpdateCarRequest request) {

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));


        carMapper.updateCar(car, request);

        return carMapper.toDetailDto(carRepository.save(car));
    }

    @Override
    public void updateCarState(Long carId, String CarStateName) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        CarState state = stateRepository.findByStatus(CarStateName)
                .orElseThrow(() -> new RuntimeException("State not found"));

        car.setState(state);
        carRepository.save(car);
    }

    @Override
    public void deleteCar(Long carId) {
        updateCarState(carId, "UNAVAILABLE");
    }

}
