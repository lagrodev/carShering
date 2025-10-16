package org.example.carshering.controller;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.FilterCarModelRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.service.CarModelService;
import org.example.carshering.service.CarService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/car")
public class CarController {

    private final CarService carService;
    private final CarModelService carModelService;

    @GetMapping("/catalogue")
    public List<CarListItemResponse> getCatalogue() {
        return carService.getAllValidCars();
    }

    @GetMapping("/{carId}")
    public CarDetailResponse findValidCar(@PathVariable Long carId) {
        return carService.findValidCar(carId);
    }


    @GetMapping("/models/{modelId}")
    public CarModelResponse getModelById(@PathVariable Long modelId) {
        return carModelService.getModelById(modelId);
    }

    @PostMapping("/models/filter")//JSON-передача через тело запроса удобнее
    public List<CarModelResponse> filterModels(@RequestBody FilterCarModelRequest request) {
        return carModelService.getModels(
                request.brand(),
                request.bodyType(),
                request.carClass()
        );
    }



}
