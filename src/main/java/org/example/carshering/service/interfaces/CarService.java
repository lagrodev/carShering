package org.example.carshering.service.interfaces;

import org.example.carshering.dto.request.CarFilterRequest;
import org.example.carshering.dto.request.create.CreateCarRequest;
import org.example.carshering.dto.request.update.UpdateCarRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.dto.response.CarStateResponse;
import org.example.carshering.dto.response.MinMaxCellForFilters;
import org.example.carshering.entity.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService  {

    CarDetailResponse getCarById(Long carId);

    Page<CarListItemResponse> getAllCars(Pageable pageable, CarFilterRequest filter);

    Car getEntity(Long carId);

    CarDetailResponse getValidCarById(Long carId, boolean favorite);


    CarDetailResponse createCar(CreateCarRequest request);


    CarDetailResponse updateCar(Long carId, UpdateCarRequest request);

    CarStateResponse updateCarState(Long carId, String CarStateName);

    void deleteCar(Long carId);

    MinMaxCellForFilters getMinMaxCell(CarFilterRequest filter);
}
