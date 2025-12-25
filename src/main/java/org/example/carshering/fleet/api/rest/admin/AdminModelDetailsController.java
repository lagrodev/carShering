package org.example.carshering.fleet.api.rest.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carshering.fleet.api.dto.request.FilterCarModelRequest;
import org.example.carshering.fleet.api.dto.request.create.CreateCarModelName;
import org.example.carshering.fleet.api.dto.request.create.CreateCarModelRequest;
import org.example.carshering.fleet.api.dto.request.create.CreateCarModelsBrand;
import org.example.carshering.fleet.api.dto.request.update.UpdateCarModelRequest;
import org.example.carshering.fleet.api.dto.responce.BrandResponse;
import org.example.carshering.fleet.api.dto.responce.CarModelResponse;
import org.example.carshering.fleet.api.dto.responce.ModelNameResponse;
import org.example.carshering.fleet.api.facade.BrandFacade;
import org.example.carshering.fleet.api.facade.CarClassFacade;
import org.example.carshering.fleet.api.facade.CarModelFacade;
import org.example.carshering.fleet.api.facade.ModelNameFacade;
import org.example.carshering.fleet.application.dto.response.BrandDto;
import org.example.carshering.fleet.application.dto.response.CarClassDto;
import org.example.carshering.fleet.application.dto.response.ModelNameDto;
import org.example.carshering.fleet.application.service.BrandApplicationService;
import org.example.carshering.fleet.application.service.CarClassApplicationService;
import org.example.carshering.fleet.application.service.CarModelApplicationService;
import org.example.carshering.fleet.application.service.ModelNameApplicationService;
import org.example.carshering.fleet.domain.valueobject.id.ModelId;
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
    private final CarModelApplicationService carModelService;

    private final BrandApplicationService carBrandService;

    private final ModelNameApplicationService carModelNameService;

    private final CarClassApplicationService carClassService;

    private final BrandFacade brandFacade;
    private final ModelNameFacade modelNameFacade;
    private final CarModelFacade carModelFacade;
    private final CarClassFacade carClassFacade;

    // модели
    @GetMapping("/models")
    @Operation(
            summary = "Get Models",
            description = "Retrieve a paginated list of car models with optional filtering by brand, body type, and class (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of car models retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)
            )
    )
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

        return carModelFacade.toResponsePage(carModelService.getAllModelsIncludingDeleted(
                filter,
                pageable));
    }

    @GetMapping("/models/{modelId}")
    @Operation(
            summary = "Get Model by ID",
            description = "Retrieve detailed information about a specific car model by its ID (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Model details retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CarModelResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Model not found"
    )
    public CarModelResponse getModelById(
            @Parameter(description = "ID of the model to retrieve", example = "1", required = true)
            @PathVariable Long modelId
    ) {
        return carModelFacade.toResponse( carModelService.getModelById(new ModelId( modelId)));
    }

    @PostMapping("/filters/brands")
    @Operation(
            summary = "Create Brand",
            description = "Create a new car brand (admin access)"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Brand created successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BrandResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request data or brand already exists"
    )
    public ResponseEntity<BrandResponse> createBrands(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Brand details to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateCarModelsBrand.class)
                    )
            )
            @RequestBody @Valid CreateCarModelsBrand updateCarModelsBrand
    ) {
        BrandResponse brandModelResponse = brandFacade.toResponse(carBrandService.createBrand(updateCarModelsBrand));

        return ResponseEntity.status(HttpStatus.CREATED).body(brandModelResponse);
    }

    @PostMapping("/filters/models")
    @Operation(
            summary = "Create Model Name",
            description = "Create a new model name (admin access)"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Model name created successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ModelNameResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request data or model name already exists"
    )
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
        ModelNameResponse brandModelResponse = modelNameFacade.toResponse( carModelNameService.createCarClass(updateCarModelsModel));

        return ResponseEntity.status(HttpStatus.CREATED).body(brandModelResponse);
    }

    @PostMapping("/filters/classes")
    @Operation(
            summary = "Create Class",
            description = "Create a new car class (admin access)"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Car class created successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ModelNameResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request data or class already exists"
    )
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
        ModelNameResponse brandModelResponse = carClassFacade.toResponse( carClassService.createCarClass(updateCarModelsModel));

        return ResponseEntity.status(HttpStatus.CREATED).body(brandModelResponse);
    }

    @GetMapping("/filters/brands")
    @Operation(
            summary = "Get Brands",
            description = "Retrieve a list of all car brands (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of brands retrieved successfully"
    )
    public List<String> getBrands(
    ) {
        return carBrandService.getAllBrands().stream().map(
                BrandDto::name
        ).toList(
        );
    }


    @GetMapping("/filters/models")
    @Operation(
            summary = "Get Model Names",
            description = "Retrieve a list of all model names (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of model names retrieved successfully"
    )
    public List<String> getModels(
    ) {
        return carModelNameService.getAllCarClass().stream().map(
                ModelNameDto::name
        ).toList(
        );
    }


    @GetMapping("/filters/classes")
    @Operation(
            summary = "Get Classes",
            description = "Retrieve a list of all car classes (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of car classes retrieved successfully"
    )
    public List<String> getClasses(
    ) {
        return carClassService.getAllCarClass().stream().map(
                CarClassDto::name
        ).toList(
        );
    }


    @PutMapping("/models/{modelId}")
    @Operation(
            summary = "Update Model",
            description = "Update the details of an existing car model by its ID (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Model updated successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CarModelResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Model not found"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
    )
    public ResponseEntity<CarModelResponse> updateModel(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated model details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateCarModelRequest.class)
                    )
            )
            @RequestBody UpdateCarModelRequest request,
            @Parameter(description = "ID of the model to update", example = "1", required = true)
            @PathVariable Long modelId
    ) {
        CarModelResponse modelResponse = carModelFacade.toResponse( carModelService.updateModel(new ModelId( modelId), request));
        return ResponseEntity.status(HttpStatus.OK).body(modelResponse);
    }


    @PostMapping("/models")
    @Operation(
            summary = "Create Model",
            description = "Create a new car model with the provided details (admin access)"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Model created successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CarModelResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
    )
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
        CarModelResponse modelResponse = carModelFacade.toResponse( carModelService.createModel(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(modelResponse);
    }

    @DeleteMapping("/models/{modelId}")
    @Operation(
            summary = "Delete Model",
            description = "Delete an existing car model by its ID (admin access)"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Model deleted successfully"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Model not found"
    )
    public ResponseEntity<?> deleteModel(
            @Parameter(description = "ID of the model to delete", example = "1", required = true)
            @PathVariable Long modelId
    ) {
        carModelService.deleteModel(new ModelId( modelId));
        return ResponseEntity.noContent().build();
    }
}
