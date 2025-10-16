package org.example.carshering.service.impl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.CreateCarModelRequest;
import org.example.carshering.dto.request.CreateCarRequest;
import org.example.carshering.dto.request.UpdateCarRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.entity.Car;
import org.example.carshering.entity.CarModel;
import org.example.carshering.entity.CarState;
import org.example.carshering.mapper.CarMapper;
import org.example.carshering.mapper.ModelMapper;
import org.example.carshering.repository.CarModelRepository;
import org.example.carshering.repository.CarRepository;
import org.example.carshering.repository.CarStateRepository;
import org.example.carshering.service.CarService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;


    private final CarStateRepository stateRepository;

    @Override
    public CarDetailResponse findCar(Long carId) {
        Car car = this.carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));
        return carMapper.toDetailDto(car);

    }

    @Override
    public List<CarListItemResponse> getAllCars() {
        return carRepository.findAll().stream()
                .map(carMapper::toListItemDto)
                .toList();
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
    public List<CarListItemResponse> getAllValidCars() {
        return carRepository.findValidCars().stream()
                .map(carMapper::toListItemDto)
                .toList();
    }





    @Override
    public CarDetailResponse createCar(CreateCarRequest request) {
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
    public CarDetailResponse updateCar(UpdateCarRequest request) {
        return null;
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

}
