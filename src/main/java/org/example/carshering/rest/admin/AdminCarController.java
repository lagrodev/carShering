package org.example.carshering.rest.admin;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.create.CreateCarRequest;
import org.example.carshering.dto.request.update.UpdateCarRequest;
import org.example.carshering.dto.request.update.UpdateCarStateRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.dto.response.CarStateResponse;
import org.example.carshering.entity.CarState;
import org.example.carshering.rest.all.CarController;
import org.example.carshering.service.CarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
            @RequestParam(value = "car_state", required = false) String carState,
            @PageableDefault(size = 20, sort = "model.brand.name") Pageable pageable
    ) {

        var filter = CarController.createFilter(brand, model, minYear, maxYear, bodyType, carClass, carState);
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
    @GetMapping("/state")
    public List<CarStateResponse> AllCarState(
    ) {
        return carService.getAllState();
    }
    //todo делете кар
    @DeleteMapping
    public ResponseEntity<?> deleteCar(
            @PathVariable Long carId
    ) {
        carService.deleteCar(carId);
        return ResponseEntity.noContent().build();
    }




}
