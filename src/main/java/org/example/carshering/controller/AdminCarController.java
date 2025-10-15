package org.example.carshering.controller;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.CreateCarModelRequest;
import org.example.carshering.dto.request.CreateCarRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.service.CarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/admin/cars")
@RestController()
public class AdminCarController {
    private final CarService carService;

    @GetMapping("/{carId}")
    public CarDetailResponse getCar(@PathVariable Long carId) {
        return carService.findCar(carId);
    }

    @GetMapping
    public List<CarListItemResponse> getAllCatalogue() {
        return carService.getAllCars();
    }

    @PostMapping
    public ResponseEntity<CarDetailResponse> createCar(@RequestBody CreateCarRequest request){
        return ResponseEntity.ok().build();
    }

    @PostMapping("/models")
    public ResponseEntity<CarModelResponse> createModel(@RequestBody CreateCarModelRequest request) {
        // логика создания модели//todo сделать

        return ResponseEntity.ok().build();
    }

}
