package org.example.carshering.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.dto.request.CarFilterRequest;
import org.example.carshering.dto.request.FilterCarModelRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.service.CarModelService;
import org.example.carshering.service.CarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/car")
public class CarController {
// todo фильтр по модели
    private final CarService carService;
    private final CarModelService carModelService;

    @GetMapping("/catalogue")
    public Page<CarListItemResponse> getCatalogue(
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "minYear", required = false) Integer minYear,
            @RequestParam(value = "maxYear", required = false) Integer maxYear,
            @RequestParam(value = "body_type", required = false) String bodyType,
            @RequestParam(value = "car_class", required = false) String carClass,
            @PageableDefault(size = 20, sort = "brand") Pageable pageable
    ) {
        var filter = new CarFilterRequest(
                brand,
                model,
                minYear,
                maxYear,
                bodyType,
                carClass
        );
        return carService.getAllValidCars(pageable, filter);
    }

    @GetMapping("/{carId}")
    public CarDetailResponse findValidCar(@PathVariable Long carId) {
        return carService.findValidCar(carId);
    }


    @GetMapping("/models/{modelId}")
    public CarModelResponse getModelById(@PathVariable Long modelId) {
        return carModelService.getModelById(modelId);
    }

    @GetMapping("/models")
    public Page<CarModelResponse> filterModels(
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "body_type", required = false) String bodyType,
            @RequestParam(value = "car_class", required = false) String carClass,
            @PageableDefault(size = 20, sort = "brand") Pageable pageable) {

        var filter = new FilterCarModelRequest(brand, bodyType, carClass);
        return carModelService.findActiveModels(
                filter,
                pageable
        );
    }



}
