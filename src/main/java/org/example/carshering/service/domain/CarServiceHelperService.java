package org.example.carshering.service.domain;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.response.CarStateResponse;
import org.example.carshering.entity.Car;
import org.example.carshering.entity.CarModel;
import org.example.carshering.entity.CarState;
import org.example.carshering.exceptions.custom.CarNotFoundException;
import org.example.carshering.repository.CarRepository;
import org.example.carshering.repository.CarStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CarServiceHelperService {

    private final CarStateRepository carStateRepository;
    private static final String CAR_STATE_UNAVAILABLE = "UNAVAILABLE";

    private final CarRepository carRepository;

    private Car getCarOrThrow(Long carId) {
        return carRepository.findById(carId)
                .orElseThrow(() -> new CarNotFoundException("Car not found"));
    }

    @Transactional
    public CarStateResponse updateCarState(Long carId, String carStateName) {
        Car car = getCarOrThrow(carId);

        CarState state = carStateRepository.findByStatusIgnoreCase(carStateName).orElseThrow(
                () -> new CarNotFoundException("Car state not found")
        );

        CarModel model = car.getModel();
        if (model.isDeleted() && !CAR_STATE_UNAVAILABLE.equals(state.getStatus())) {
            model.setDeleted(false);
        }


        car.setState(state);
        carRepository.save(car);
        return new  CarStateResponse(state.getId(), carStateName);
    }



    @Transactional
    public void deleteCar(Long carId) {
        updateCarState(carId, CAR_STATE_UNAVAILABLE);
    }


    public Car getEntity(Long carId) {
        return getCarOrThrow(carId);
    }
}
