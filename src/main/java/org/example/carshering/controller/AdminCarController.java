package org.example.carshering.controller;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.*;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.service.CarModelService;
import org.example.carshering.service.CarService;
import org.hibernate.sql.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public List<CarListItemResponse> getCars() {
        return carService.getAllCars();
    }

    @PostMapping
    public ResponseEntity<CarDetailResponse> createCar(@RequestBody CreateCarRequest request){
        CarDetailResponse car = carService.createCar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(car);
    }

    @PatchMapping
    public ResponseEntity<CarDetailResponse> updateCar(@RequestBody UpdateCarRequest request){
        CarDetailResponse car = carService.updateCar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(car);
    }

    @PatchMapping("/{carId}/state")
    public ResponseEntity<?> updateCarState(// это же для мягкого удаления
            @PathVariable Long carId,
            @RequestBody UpdateCarStateRequest request
    ) {
        carService.updateCarState(carId, request.stateName());
        return ResponseEntity.noContent().build();
    }


    // модели

    @PostMapping("/models")
    public ResponseEntity<CarModelResponse> createModel(@RequestBody CreateCarModelRequest request) {
        //todo сделать
        CarModelResponse modelResponse = carModelService.createModel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelResponse);
    }

    @DeleteMapping("/models/{modelId}")
    public ResponseEntity<?> deleteModel(@PathVariable Long modelId) {
        carModelService.deleteModel(modelId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/models/filter")
    // todo фильтр в репозитории доп. ерунда прописана, надо сделать
    public List<CarModelResponse> filterAllModels(@RequestBody FilterCarModelRequest request) {
        return carModelService.getAllModelsIncludingDeleted(request.brand(),request.bodyType(), request.carClass());
    }


}
