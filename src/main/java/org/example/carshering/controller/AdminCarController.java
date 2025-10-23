package org.example.carshering.controller;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.*;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.service.CarModelService;
import org.example.carshering.service.CarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/admin/cars")
@RestController()
public class AdminCarController {

    private final CarService carService;
    private final CarModelService carModelService;

    @GetMapping("/{carId}")
    public CarDetailResponse getCar(@PathVariable Long carId) {
        return carService.findCar(carId);
    }

    @PatchMapping("/{carId}")
    public ResponseEntity<CarDetailResponse> updateCar(
            @PathVariable Long carId,
            @RequestBody UpdateCarRequest request
    ) {
        CarDetailResponse car = carService.updateCar(carId, request);
        return ResponseEntity.status(HttpStatus.OK).body(car);
    }

    @GetMapping
    public Page<CarListItemResponse> getCars(
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "minYear", required = false) Integer minYear,
            @RequestParam(value = "maxYear", required = false) Integer maxYear,
            @RequestParam(value = "body_type", required = false) String bodyType,
            @RequestParam(value = "car_class", required = false) String carClass,
            @PageableDefault(size = 20, sort = "brand") Pageable pageable
    ) {

        var filter = CarController.createFilter(brand, model, minYear, maxYear, bodyType, carClass);
        return carService.getAllCars(pageable, filter);
    }

    @PostMapping
    public ResponseEntity<CarDetailResponse> createCar(
            @RequestBody CreateCarRequest request
    ) {
        CarDetailResponse car = carService.createCar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(car);
    }


    @PatchMapping("/{carId}/state")
    public ResponseEntity<?> updateCarState( // это же для мягкого удаления, так, а тут надо убрать Invalaible
                                             @PathVariable Long carId,
                                             @RequestBody UpdateCarStateRequest request
    ) {
        carService.updateCarState(carId, request.stateName());
        return ResponseEntity.noContent().build();
    }

    //todo делете кар
    @DeleteMapping
    public ResponseEntity<?> deleteCar(
            @PathVariable Long carId
    ) {
        carService.deleteCar(carId);
        return ResponseEntity.noContent().build();
    }


    // модели
    @GetMapping("/models")
    // todo менять
    // todo фильтр в репозитории доп. ерунда прописана, надо сделать
    public Page<CarModelResponse> allModels(
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "body_type", required = false) String bodyType,
            @RequestParam(value = "car_class", required = false) String carClass,
            @PageableDefault(size = 20, sort = "brand") Pageable pageable
    ) {
        var filter = new FilterCarModelRequest(brand, bodyType, carClass);

        return carModelService.getAllModelsIncludingDeleted(
                filter,
                pageable);
    }

    // todo updateModel

    @PatchMapping("/models/{modelId}")
    public ResponseEntity<CarModelResponse> updateModel(
            @RequestBody UpdateCarModelRequest request,
            @PathVariable Long modelId
    ) {
        //todo сделать
        CarModelResponse modelResponse = carModelService.updateModel(modelId, request);
        return ResponseEntity.status(HttpStatus.OK).body(modelResponse);
    }


    @PostMapping("/models")
    public ResponseEntity<CarModelResponse> createModel(
            @RequestBody CreateCarModelRequest request
    ) {
        //todo сделать
        CarModelResponse modelResponse = carModelService.createModel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelResponse);
    }

    @DeleteMapping("/models/{modelId}")
    public ResponseEntity<?> deleteModel(@PathVariable Long modelId) {
        carModelService.deleteModel(modelId);
        return ResponseEntity.noContent().build();
    }


}
