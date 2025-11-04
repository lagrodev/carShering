package org.example.carshering.rest.admin;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.create.CreateCarModelName;
import org.example.carshering.dto.request.create.CreateCarModelRequest;
import org.example.carshering.dto.request.FilterCarModelRequest;
import org.example.carshering.dto.request.create.CreateCarModelsBrand;
import org.example.carshering.dto.request.update.UpdateCarModelRequest;
import org.example.carshering.dto.response.BrandModelResponse;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.dto.response.ModelNameResponse;
import org.example.carshering.service.CarBrandService;
import org.example.carshering.service.CarClassService;
import org.example.carshering.service.CarModelNameService;
import org.example.carshering.service.CarModelService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController

@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminModelDetailsController {
    private final CarModelService carModelService;

    // модели
    @GetMapping("/models")
    // todo менять
    // todo фильтр в репозитории доп. ерунда прописана, надо сделать
    public Page<CarModelResponse> allModels(
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "body_type", required = false) String bodyType,
            @RequestParam(value = "car_class", required = false) String carClass,
            @PageableDefault(size = 20, sort = "brand.name") Pageable pageable
    ) {
        var filter = new FilterCarModelRequest(brand, bodyType, carClass, false);

        return carModelService.getAllModelsIncludingDeleted(
                filter,
                pageable);
    }

    @GetMapping("/models/{modelId}")
    public CarModelResponse getModelById(@PathVariable Long modelId) {
        return carModelService.getModelById(modelId);
    }

    private final CarBrandService carBrandService;

    @PostMapping("/filters/brands")
    public ResponseEntity<?> createBrands(
            @RequestBody CreateCarModelsBrand updateCarModelsBrand
    ) {
        BrandModelResponse brandModelResponse = carBrandService.createBrands(updateCarModelsBrand);

        return ResponseEntity.status(HttpStatus.CREATED).body(brandModelResponse);
    }

    @PostMapping("/filters/models")
    public ResponseEntity<?> createModels(
            @RequestBody CreateCarModelName updateCarModelsModel
    ) {
        ModelNameResponse brandModelResponse = carModelNameService.createModelName(updateCarModelsModel);

        return ResponseEntity.status(HttpStatus.CREATED).body(brandModelResponse);
    }
private final CarModelNameService carModelNameService;



    @PostMapping("/filters/classes")
    public ResponseEntity<?> createClasses(
            @RequestBody CreateCarModelName updateCarModelsModel
    ) {
        ModelNameResponse brandModelResponse = carClassService.createCarClass(updateCarModelsModel);

        return ResponseEntity.status(HttpStatus.CREATED).body(brandModelResponse);
    }
    private final CarClassService carClassService;



    @GetMapping("/filters/brands")
    public List<String> getBrands(
    ) {
        return carBrandService.findAllBrands();
    }


    @GetMapping("/filters/models")
    public List<String> getModels(
    ) {
        return carModelNameService.findAllModels();
    }


    @GetMapping("/filters/classes")
    public List<String> getClasses(
    ) {
        return carClassService.findAllClasses();
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
