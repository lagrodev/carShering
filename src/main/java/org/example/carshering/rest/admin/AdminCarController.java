package org.example.carshering.rest.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.create.CreateCarRequest;
import org.example.carshering.dto.request.update.UpdateCarRequest;
import org.example.carshering.dto.request.update.UpdateCarStateRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.dto.response.CarStateResponse;
import org.example.carshering.rest.all.CarController;
import org.example.carshering.service.interfaces.CarService;
import org.example.carshering.service.interfaces.CarStateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/admin/cars")
@Tag(name = "Admin Car Management", description = "Endpoints for admin car management")
@RestController()
public class AdminCarController {

    private final CarService carService;
    private final CarStateService carStateService;

    @GetMapping("/{carId}")
    @Operation(
            summary = "Get Car by ID",
            description = "Retrieve detailed information about a specific car by its ID (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Car details retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CarDetailResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Car not found"
    )
    public ResponseEntity<?> getCar(
            @Parameter(
                    description = "ID of the car to retrieve",
                    example = "1",
                    required = true
            ) @PathVariable Long carId
    ) {
        return ResponseEntity.ok(carService.getCarById(carId));
    }

    @Operation(
            summary = "Update Car",
            description = "Update the details of an existing car by its ID (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Car updated successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CarDetailResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Car not found"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
    )
    @PatchMapping("/{carId}")
    public ResponseEntity<CarDetailResponse> updateCar(
            @Parameter(
                    description = "ID of the car to update",
                    example = "1",
                    required = true
            ) @PathVariable Long carId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated car details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateCarRequest.class)
                    )
            ) @RequestBody @Valid UpdateCarRequest request
    ) {
        CarDetailResponse car = carService.updateCar(carId, request);
        return ResponseEntity.ok(car);
    }


    @Operation(
            summary = "Create Car",
            description = "Create a new car with the provided details (admin access)"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Car created successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CarDetailResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
    )
    @PostMapping
    public ResponseEntity<CarDetailResponse> createCar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details of the car to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateCarRequest.class)
                    )
            ) @RequestBody @Valid CreateCarRequest request
    ) {
        CarDetailResponse car = carService.createCar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(car);
    }


    @Operation(
            summary = "Delete Car",
            description = "Delete an existing car by its ID (admin access)"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Car deleted successfully"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Car not found"
    )
    @DeleteMapping({"/{carId}"})
    public ResponseEntity<?> deleteCar(
            @Parameter(
                    description = "ID of the car to delete",
                    example = "1",
                    required = true
            ) @PathVariable Long carId
    ) {
        carService.deleteCar(carId);
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Update Car State",
            description = "Update the state of a specific car by its ID (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Car state updated successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CarStateResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Car not found"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid state name"
    )
    @PatchMapping("/{carId}/state")
    public ResponseEntity<?> updateCarState(
                                             @Parameter(
                                                     description = "ID of the car to update state",
                                                     example = "1",
                                                     required = true
                                             ) @PathVariable Long carId,
                                             @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                     description = "New state for the car",
                                                     required = true,
                                                     content = @Content(
                                                             schema = @Schema(implementation = UpdateCarStateRequest.class)
                                                     )
                                             ) @RequestBody @Valid UpdateCarStateRequest request
    ) {
        CarStateResponse carStateResponse = carService.updateCarState(carId, request.stateName());
        return ResponseEntity.status(HttpStatus.OK).body(carStateResponse);
    }


    @Operation(
            summary = "Get Cars",
            description = "Retrieve a paginated list of all cars with optional filtering parameters (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of cars retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)
            )
    )
    @GetMapping
    public Page<CarListItemResponse> getCars(
            @Parameter(
                    description = "Filter by car brand",
                    example = "Toyota"
            ) @RequestParam(value = "brand", required = false) String brand,
            @Parameter(
                    description = "Filter by car model",
                    example = "Camry"
            ) @RequestParam(value = "model", required = false) String model,
            @Parameter(
                    description = "Minimum manufacturing year",
                    example = "2015"
            ) @RequestParam(value = "minYear", required = false) Integer minYear,
            @Parameter(
                    description = "Maximum manufacturing year",
                    example = "2023"
            ) @RequestParam(value = "maxYear", required = false) Integer maxYear,
            @Parameter(
                    description = "Filter by body type",
                    example = "Sedan"
            ) @RequestParam(value = "body_type", required = false) String bodyType,
            @Parameter(
                    description = "Filter by car class",
                    example = "Economy"
            ) @RequestParam(value = "car_class", required = false) String carClass,
            @Parameter(
                    description = "Filter by car state",
                    example = "AVAILABLE"
            ) @RequestParam(value = "car_state", required = false) String carState,
            @Parameter(
                    description = "Start date for car availability",
                    example = "2025-01-01"
            ) @RequestParam(value = "date_start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateStart,
            @Parameter(
                    description = "End date for car availability",
                    example = "2025-01-31"
            ) @RequestParam(value = "date_end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateEnd,
            @Parameter(
                    description = "Minimum price per day",
                    example = "1000"
            ) @RequestParam(value = "min_cell", required = false) Double minCell,
            @Parameter(
                    description = "Maximum price per day",
                    example = "5000"
            ) @RequestParam(value = "max_cell", required = false) Double maxCell,

            @Parameter(
                    description = "Pagination and sorting information"
            ) @PageableDefault(size = 20, sort = "model.brand.name") Pageable pageable
    ) {

        var filter = CarController.createFilter(brand, model, minYear, maxYear, bodyType, carClass, carState, dateStart, dateEnd, minCell, maxCell);
        return carService.getAllCars(pageable, filter);
    }

    @Operation(
            summary = "Get All Car States",
            description = "Retrieve a list of all possible car states (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of car states retrieved successfully"
    )
    @GetMapping("/state")
    public List<CarStateResponse> AllCarStates(
    ) {
        return carStateService.getAllStates();
    }

}
