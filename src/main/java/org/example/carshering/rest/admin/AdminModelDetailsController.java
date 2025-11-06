package org.example.carshering.rest.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.FilterCarModelRequest;
import org.example.carshering.dto.request.create.CreateCarModelName;
import org.example.carshering.dto.request.create.CreateCarModelRequest;
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
@Tag(name = "Admin Model Details Management", description = "Endpoints for admin model details management")
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminModelDetailsController {
    private final CarModelService carModelService;
    private final CarBrandService carBrandService;
    private final CarModelNameService carModelNameService;
    private final CarClassService carClassService;

    // модели
    @GetMapping("/models")
    @Operation(
            summary = "Get Models",
            description = "Retrieve a paginated list of car models with optional filtering"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of car models retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CarModelResponse.class)
            )
    )
    @Tag(name = "get-models")
    @Tag(name = "Get Models", description = "Retrieve a paginated list of car models with optional filtering")
    // todo фильтр в репозитории доп. ерунда прописана, надо сделать
    public Page<CarModelResponse> allModels(
            @Parameter(description = "Filter by brand name", example = "Toyota")
            @RequestParam(value = "brand", required = false) String brand,
            @Parameter(description = "Filter by body type", example = "Sedan")
            @RequestParam(value = "body_type", required = false) String bodyType,
            @Parameter(description = "Filter by car class", example = "Business")
            @RequestParam(value = "car_class", required = false) String carClass,
            @Parameter(description = "Pagination and sorting information")
            @PageableDefault(size = 20, sort = "brand.name") Pageable pageable
    ) {
        var filter = new FilterCarModelRequest(brand, bodyType, carClass, false);

        return carModelService.getAllModelsIncludingDeleted(
                filter,
                pageable);
    }

    @GetMapping("/models/{modelId}")
    @Operation(
            summary = "Get Model by ID",
            description = "Retrieve detailed information about a specific model by its ID"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Model details retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CarModelResponse.class)
            )
    )
    @Tag(name = "get-model-by-id")
    @Tag(name = "Get Model by ID", description = "Retrieve detailed information about a specific model by its ID")
    public CarModelResponse getModelById(
            @Parameter(description = "ID of the model to retrieve", example = "1")
            @PathVariable Long modelId
    ) {
        return carModelService.getModelById(modelId);
    }

    @PostMapping("/filters/brands")
    @Operation(
            summary = "Create Brand",
            description = "Create a new car brand"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Brand created successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BrandModelResponse.class)
            )
    )
    @Tag(name = "create-brand")
    @Tag(name = "Create Brand", description = "Create a new car brand")
    public ResponseEntity<?> createBrands(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Brand details to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateCarModelsBrand.class)
                    )
            )
            @RequestBody @Valid CreateCarModelsBrand updateCarModelsBrand
    ) {
        BrandModelResponse brandModelResponse = carBrandService.createBrands(updateCarModelsBrand);

        return ResponseEntity.status(HttpStatus.CREATED).body(brandModelResponse);
    }

    @PostMapping("/filters/models")
    @Operation(
            summary = "Create Model Name",
            description = "Create a new model name"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Model name created successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ModelNameResponse.class)
            )
    )
    @Tag(name = "create-model-name")
    @Tag(name = "Create Model Name", description = "Create a new model name")
    public ResponseEntity<?> createModels(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Model name to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateCarModelName.class)
                    )
            )
            @RequestBody @Valid CreateCarModelName updateCarModelsModel
    ) {
        ModelNameResponse brandModelResponse = carModelNameService.createModelName(updateCarModelsModel);

        return ResponseEntity.status(HttpStatus.CREATED).body(brandModelResponse);
    }

    @PostMapping("/filters/classes")
    @Operation(
            summary = "Create Class",
            description = "Create a new car class"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Car class created successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ModelNameResponse.class)
            )
    )
    @Tag(name = "create-class")
    @Tag(name = "Create Class", description = "Create a new car class")
    public ResponseEntity<?> createClasses(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Car class to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateCarModelName.class)
                    )
            )
            @RequestBody @Valid CreateCarModelName updateCarModelsModel
    ) {
        ModelNameResponse brandModelResponse = carClassService.createCarClass(updateCarModelsModel);

        return ResponseEntity.status(HttpStatus.CREATED).body(brandModelResponse);
    }

    @GetMapping("/filters/brands")
    @Operation(
            summary = "Get Brands",
            description = "Retrieve a list of all car brands"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of brands retrieved successfully"
    )
    @Tag(name = "get-brands")
    @Tag(name = "Get Brands", description = "Retrieve a list of all car brands")
    public List<String> getBrands(
    ) {
        return carBrandService.findAllBrands();
    }


    @GetMapping("/filters/models")
    @Operation(
            summary = "Get Model Names",
            description = "Retrieve a list of all model names"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of model names retrieved successfully"
    )
    @Tag(name = "get-model-names")
    @Tag(name = "Get Model Names", description = "Retrieve a list of all model names")
    public List<String> getModels(
    ) {
        return carModelNameService.findAllModels();
    }


    @GetMapping("/filters/classes")
    @Operation(
            summary = "Get Classes",
            description = "Retrieve a list of all car classes"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of car classes retrieved successfully"
    )
    @Tag(name = "get-classes")
    @Tag(name = "Get Classes", description = "Retrieve a list of all car classes")
    public List<String> getClasses(
    ) {
        return carClassService.findAllClasses();
    }


    @PutMapping("/models/{modelId}")
    @Operation(
            summary = "Update Model",
            description = "Update the details of an existing model by its ID"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Model updated successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CarModelResponse.class)
            )
    )
    @Tag(name = "update-model")
    @Tag(name = "Update Model", description = "Update the details of an existing model by its ID")
    public ResponseEntity<CarModelResponse> updateModel(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated model details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateCarModelRequest.class)
                    )
            )
            @RequestBody @Valid  UpdateCarModelRequest request,
            @Parameter(description = "ID of the model to update", example = "1")
            @PathVariable Long modelId
    ) {
        CarModelResponse modelResponse = carModelService.updateModel(modelId, request);
        return ResponseEntity.status(HttpStatus.OK).body(modelResponse);
    }


    @PostMapping("/models")
    @Operation(
            summary = "Create Model",
            description = "Create a new car model with the provided details"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Model created successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CarModelResponse.class)
            )
    )
    @Tag(name = "create-model")
    @Tag(name = "Create Model", description = "Create a new car model with the provided details")
    public ResponseEntity<CarModelResponse> createModel(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Model details to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateCarModelRequest.class)
                    )
            )
            @RequestBody @Valid CreateCarModelRequest request
    ) {
        CarModelResponse modelResponse = carModelService.createModel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelResponse);
    }

    @DeleteMapping("/models/{modelId}")
    @Operation(
            summary = "Delete Model",
            description = "Delete an existing model by its ID"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Model deleted successfully"
    )
    @Tag(name = "delete-model")
    @Tag(name = "Delete Model", description = "Delete an existing model by its ID")
    public ResponseEntity<?> deleteModel(
            @Parameter(description = "ID of the model to delete", example = "1")
            @PathVariable Long modelId
    ) {
        carModelService.deleteModel(modelId);
        return ResponseEntity.noContent().build();
    }
}
