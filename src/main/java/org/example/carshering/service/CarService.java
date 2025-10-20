package org.example.carshering.service;

import org.example.carshering.dto.request.CarFilterRequest;
import org.example.carshering.dto.request.CreateCarRequest;
import org.example.carshering.dto.request.UpdateCarRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.entity.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService  {
    CarDetailResponse findCar(Long carId);
    Page<CarListItemResponse> getAllCars(Pageable pageable, CarFilterRequest filter);
    Car getEntity(Long carId);
    CarDetailResponse findValidCar(Long carId);
    Page<CarListItemResponse> getAllValidCars(Pageable pageable, CarFilterRequest filter);


    CarDetailResponse createCar(CreateCarRequest request);


    CarDetailResponse updateCar(Long carId, UpdateCarRequest request);
    void updateCarState(Long carId,String CarStateName);

    void deleteCar(Long carId);
}
