package org.example.carshering.fleet.application.service;

import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.dto.request.CarFilterRequest;
import org.example.carshering.dto.request.create.CreateCarRequest;
import org.example.carshering.dto.request.update.UpdateCarRequest;
import org.example.carshering.fleet.api.dto.responce.CarStateResponse;
import org.example.carshering.fleet.api.dto.responce.MinMaxCellForFilters;
import org.example.carshering.fleet.application.dto.response.CarDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface CarApplicationService {
    @Transactional
    CarDto createCar(CreateCarRequest request);

    Page<CarDto> getAllCars(Pageable pageable, CarFilterRequest filter);

    CarDto getCarById(CarId carId);

    CarDto getValidCarById(Long carId, boolean favorite);

    @Transactional
    CarDto updateCar(Long carId, UpdateCarRequest request);

    @Transactional
    CarDto updateCarState(Long carId, String CarStateName);

    @Transactional
    void deleteCar(Long carId);

    MinMaxCellForFilters getMinMaxCell(CarFilterRequest filter);


    List<String> getAllStates();
}
