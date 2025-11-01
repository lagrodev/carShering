package org.example.carshering.service;

import org.example.carshering.dto.request.CarFilterRequest;
import org.example.carshering.dto.request.create.CreateCarRequest;
import org.example.carshering.dto.request.update.UpdateCarRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.dto.response.CarStateResponse;
import org.example.carshering.entity.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CarService  {
    CarDetailResponse findCar(Long carId);
    Page<CarListItemResponse> getAllCars(Pageable pageable, CarFilterRequest filter);
    Car getEntity(Long carId);
    CarDetailResponse findValidCar(Long carId);


    CarDetailResponse createCar(CreateCarRequest request);


    CarDetailResponse updateCar(Long carId, UpdateCarRequest request);
    void updateCarState(Long carId,String CarStateName);

    void deleteCar(Long carId);

    List<CarStateResponse> getAllState();
}
