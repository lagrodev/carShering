package org.example.carshering.rest.all;

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

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/car")
public class CarController {
    // todo фильтр по модели
    private final CarService carService;
    private final CarModelService carModelService;

    public static CarFilterRequest createFilter(@RequestParam(value = "brand", required = false) String brand,
                                                @RequestParam(value = "model", required = false) String model,
                                                @RequestParam(value = "minYear", required = false) Integer minYear,
                                                @RequestParam(value = "maxYear", required = false) Integer maxYear,
                                                @RequestParam(value = "body_type", required = false) String bodyType,
                                                @RequestParam(value = "car_class", required = false) String carClass,
                                                @RequestParam(value = "car_state", required = false) String carState) {
        List<String> brands = brand != null ? Arrays.asList(brand.split(",")) : List.of();
        List<String> models = model != null ? Arrays.asList(model.split(",")) : List.of();
        List<String> carClasses = carClass != null ? Arrays.asList(carClass.split(",")) : List.of();
        List<String> carStates = carState != null ? Arrays.asList(carState.split(",")) : List.of();

        return new CarFilterRequest(
                brands,
                models,
                minYear,
                maxYear,
                bodyType,
                carClasses,
                carStates
        );
    }

    @GetMapping("/catalogue")
    public Page<CarListItemResponse> getCatalogue(
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "minYear", required = false) Integer minYear,
            @RequestParam(value = "maxYear", required = false) Integer maxYear,
            @RequestParam(value = "body_type", required = false) String bodyType,
            @RequestParam(value = "car_class", required = false) String carClass,
            @PageableDefault(size = 20, sort = "model.brand.name") Pageable pageable
    ) {
        //todo: узнать  у нее - это норма?
        var filter = createFilter(brand, model, minYear, maxYear, bodyType, carClass, "AVAILABLE");
        return carService.getAllCars(pageable, filter);
    }

    @GetMapping("/{carId}")
    public CarDetailResponse findValidCar(@PathVariable Long carId) {
        return carService.findValidCar(carId);
    }



    @GetMapping("/filters/brands")
    public List<String> getBrands() {
        return carModelService.findAllBrands();
    }

    @GetMapping("/filters/models")
    public List<String> getModels() {
        return carModelService.findAllModels();
    }

    @GetMapping("/filters/classes")
    public List<String> getClasses() {
        return carModelService.findAllClasses();
    }

    @GetMapping("/filters/body-types")
    public List<String> getBodyTypes() {
        return carModelService.findAllBodyTypes();
    }


}
