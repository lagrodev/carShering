package org.example.carshering.service;

import org.example.carshering.dto.request.CreateCarRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.entity.Car;

import java.util.List;

public interface CarService  {
    CarDetailResponse findCar(Long carId);
    List<CarListItemResponse> getAllCars();
    Car getEntity(Long carId);
    CarDetailResponse findValidCar(Long carId);
    List<CarListItemResponse> getAllValidCars();


    CarDetailResponse createCar(CreateCarRequest request);

}
