package org.example.carshering.controller;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.service.CarService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/car")
public class CarController {

    private final CarService carService;

    @GetMapping("/catalogue")
    public List<CarListItemResponse> getCatalogue() {
        return carService.getAllValidCars();
    }




    @GetMapping("/admin/{carId}")
    public CarDetailResponse findValidCar(@PathVariable Long carId) {
        return carService.findValidCar(carId);
    }

}
