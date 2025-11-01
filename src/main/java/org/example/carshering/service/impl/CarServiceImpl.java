package org.example.carshering.service.impl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.CarFilterRequest;
import org.example.carshering.dto.request.create.CreateCarRequest;
import org.example.carshering.dto.request.update.UpdateCarRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.dto.response.CarStateResponse;
import org.example.carshering.entity.Car;
import org.example.carshering.entity.CarModel;
import org.example.carshering.entity.CarState;
import org.example.carshering.mapper.CarMapper;
import org.example.carshering.mapper.CarStateMapper;
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
        List<String> carStates = isEmpty(filter.carState()) ? null : filter.carState();

        return carRepository.findByFilter(
                brands,
                models,
                filter.minYear(),
                filter.maxYear(),
                filter.bodyType(),
                carClasses,
                carStates,
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
            "id",
            "gosNumber",
            "yearOfIssue",
            "rent",
            "model.bodyType",
            "model.brand.name",
            "model.model.name",         
            "model.carClass.name"
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

        Car car = carMapper.toEntity(request);

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

        System.out.println("model id в апдейте: " + request.modelId());

        CarModel newModel = carModelRepository.findById(request.modelId())
                .orElseThrow(() -> new RuntimeException("CarModel with id " + request.modelId() + " not found"));

        System.out.println("new model: " + newModel.getModel().getName());

        car.setModel(newModel);

        CarDetailResponse carDetailResponse =  carMapper.toDetailDto(carRepository.save(car));
        System.out.println(carDetailResponse + " обновлен успешно");
        return carDetailResponse;
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
        System.out.println("удаление успешно");
        updateCarState(carId, "UNAVAILABLE");
    }


    private final CarStateMapper stateMapper;

    @Override
    public List<CarStateResponse> getAllState() {
        return stateRepository.findAll().
                stream()
                .map(stateMapper::toDto)
                .toList();
    }

}
