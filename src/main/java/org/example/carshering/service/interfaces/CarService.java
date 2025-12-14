package org.example.carshering.service.interfaces;

import org.example.carshering.dto.request.CarFilterRequest;
import org.example.carshering.dto.request.create.CreateCarRequest;
import org.example.carshering.dto.request.update.UpdateCarRequest;
import org.example.carshering.fleet.api.dto.responce.CarDetailResponse;
import org.example.carshering.fleet.api.dto.responce.CarListItemResponse;
import org.example.carshering.fleet.api.dto.responce.CarStateResponse;
import org.example.carshering.fleet.api.dto.responce.MinMaxCellForFilters;
import org.example.carshering.fleet.infrastructure.persistence.entity.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface CarService  {

    @Transactional(readOnly = true)
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
