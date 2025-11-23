package org.example.carshering.rest.all;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.dto.request.CarFilterRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.dto.response.MinMaxCellForFilters;
import org.example.carshering.security.ClientDetails;
import org.example.carshering.service.interfaces.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@Tag(name = "Car Catalogue", description = "Endpoints for browsing available cars and filters")
@RequiredArgsConstructor
@RequestMapping("api/car")
public class CarController {
    // todo фильтр по модели
    private final CarService carService;
    private final CarModelService carModelService;
    private final CarBrandService carBrandService;
    private final CarModelNameService carModelNameService;
    private final CarClassService carClassService;
    private final FavoriteService favoriteService;

    public static CarFilterRequest createFilter(String brand,
                                                String model,
                                                Integer minYear,
                                                Integer maxYear,
                                                String bodyType,
                                                String carClass,
                                                String carState,
                                                LocalDate dateStart,
                                                LocalDate dateEnd,
                                                Double minCell,
                                                Double maxCell
    ) {
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
                carStates,
                dateStart,
                dateEnd,
                minCell,
                maxCell
        );
    }

    @GetMapping("/catalogue")
    @Operation(
            summary = "Get Car Catalogue",
            description = "Retrieve a paginated list of available cars with optional filtering"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of available cars retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CarListItemResponse.class)
            )
    )
    @Tag(name = "get-car-catalogue")
    @Tag(name = "Get Car Catalogue", description = "Retrieve a paginated list of available cars with optional filtering")
    public Page<CarListItemResponse> getCatalogue(
            @Parameter(description = "Filter by car brand", example = "Toyota")
            @RequestParam(value = "brand", required = false) String brand,
            @Parameter(description = "Filter by car model", example = "Camry")
            @RequestParam(value = "model", required = false) String model,
            @Parameter(description = "Minimum year of issue", example = "2015")
            @RequestParam(value = "minYear", required = false) Integer minYear,
            @Parameter(description = "Maximum year of issue", example = "2023")
            @RequestParam(value = "maxYear", required = false) Integer maxYear,
            @Parameter(description = "Filter by body type", example = "Sedan")
            @RequestParam(value = "body_type", required = false) String bodyType,
            @Parameter(description = "Filter by car class", example = "Business")
            @RequestParam(value = "car_class", required = false) String carClass,
            @Parameter(description = "Start date for car availability", example = "2025-01-01")
            @RequestParam(value = "date_start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateStart,
            @Parameter(description = "End date for car availability", example = "2025-01-31")
            @RequestParam(value = "date_end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEnd,
            @Parameter(description = "Minimum price per day", example = "1000")
            @RequestParam(value = "min_cell", required = false) Double minCell,
            @Parameter(description = "Maximum price per day", example = "5000")
            @RequestParam(value = "max_cell", required = false) Double maxCell,
            @Parameter(description = "Pagination and sorting information")
            @PageableDefault(size = 20, sort = "model.brand.name") Pageable pageable,
            @Parameter(hidden = true) Authentication auth
    ) {
        //todo: узнать  у нее - это норма?
        var filter = createFilter(brand, model, minYear, maxYear, bodyType, carClass, "AVAILABLE", dateStart, dateEnd, minCell, maxCell);
        var carsPage = carService.getAllCars(pageable, filter);
        if (auth != null) {
            log.info("User '{}' is browsing the car catalogue with filters: brand='{}', model='{}', minYear='{}', maxYear='{}', bodyType='{}', carClass='{}', dateStart='{}', dateEnd='{}', minCell='{}', maxCell='{}'.",
                    auth.getName(), brand, model, minYear, maxYear, bodyType, carClass, dateStart, dateEnd, minCell, maxCell);
            Set<Long> favorites = favoriteService.getAllFavoriteCarIds(((ClientDetails) auth.getPrincipal()).getId());

            log.info("User '{}' has {} favorite cars.", auth.getName(), favorites.size());
            carsPage = carsPage.map(carDto -> {
                boolean isFav = favorites.contains(carDto.id());
                return new CarListItemResponse(
                        carDto.id(),
                        carDto.brand(),
                        carDto.carClass(),
                        carDto.model(),
                        carDto.yearOfIssue(),
                        carDto.rent(),
                        carDto.status(),
                        isFav
                );
            });

        }

        return carsPage;
    }

    @GetMapping("/filters/min-max-cell")
    @Operation(
            summary = "Get Min and Max Cell for Filters",
            description = "Retrieve the minimum and maximum rental prices per day for available cars based on filters"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Minimum and maximum rental prices retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MinMaxCellForFilters.class)
            )
    )
    @Tag(name = "get-min-max-cell-for-filters")
    @Tag(name = "Get Min and Max Cell for Filters", description = "Retrieve the minimum and maximum rental prices per day for available cars based on filters")
    public MinMaxCellForFilters getMaxCell(
            @Parameter(description = "Filter by car brand", example = "Toyota")
            @RequestParam(value = "brand", required = false) String brand,
            @Parameter(description = "Filter by car model", example = "Camry")
            @RequestParam(value = "model", required = false) String model,
            @Parameter(description = "Minimum year of issue", example = "2015")
            @RequestParam(value = "minYear", required = false) Integer minYear,
            @Parameter(description = "Maximum year of issue", example = "2023")
            @RequestParam(value = "maxYear", required = false) Integer maxYear,
            @Parameter(description = "Filter by body type", example = "Sedan")
            @RequestParam(value = "body_type", required = false) String bodyType,
            @Parameter(description = "Filter by car class", example = "Business")
            @RequestParam(value = "car_class", required = false) String carClass,
            @Parameter(description = "Start date for car availability", example = "2025-01-01")
            @RequestParam(value = "date_start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateStart,
            @Parameter(description = "End date for car availability", example = "2025-01-31")
            @RequestParam(value = "date_end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEnd
    ) {
        var filter = createFilter(brand, model, minYear, maxYear, bodyType, carClass, "AVAILABLE", dateStart, dateEnd, null, null);
        return carService.getMinMaxCell(filter);
    }


    @GetMapping("/{carId}")
    @Operation(
            summary = "Get Valid Car",
            description = "Retrieve detailed information about a specific available car by its ID"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Car details retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CarDetailResponse.class)
            )
    )
    @Tag(name = "get-valid-car")
    @Tag(name = "Get Valid Car", description = "Retrieve detailed information about a specific available car by its ID")
    public CarDetailResponse findValidCar(
            @Parameter(description = "ID of the car to retrieve", example = "1")
            @PathVariable Long carId,
            @Parameter(hidden = true) Authentication auth
    ) {
        if (auth != null) {
            var car = favoriteService.getFavorite(((ClientDetails) auth.getPrincipal()).getId(), carId);
            if (car != null) {
                return carService.getValidCarById(carId, true);
            }
        }

        return carService.getValidCarById(carId, false);
    }

    @GetMapping("/filters/brands")
    @Operation(
            summary = "Get Filter Brands",
            description = "Retrieve a list of all car brands for filtering"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of brands retrieved successfully"
    )
    @Tag(name = "get-filter-brands")
    @Tag(name = "Get Filter Brands", description = "Retrieve a list of all car brands for filtering")
    public List<String> getBrands() {
        return carBrandService.findAllBrands();
    }

    @GetMapping("/filters/models")
    @Operation(
            summary = "Get Filter Models",
            description = "Retrieve a list of all model names for filtering"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of model names retrieved successfully"
    )
    @Tag(name = "get-filter-models")
    @Tag(name = "Get Filter Models", description = "Retrieve a list of all model names for filtering")
    public List<String> getModels() {
        return carModelNameService.findAllModels();
    }

    @GetMapping("/filters/classes")
    @Operation(
            summary = "Get Filter Classes",
            description = "Retrieve a list of all car classes for filtering"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of car classes retrieved successfully"
    )
    @Tag(name = "get-filter-classes")
    @Tag(name = "Get Filter Classes", description = "Retrieve a list of all car classes for filtering")
    public List<String> getClasses() {
        return carClassService.findAllClasses();
    }

    @GetMapping("/filters/body-types")
    @Operation(
            summary = "Get Filter Body Types",
            description = "Retrieve a list of all body types for filtering"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of body types retrieved successfully"
    )
    @Tag(name = "get-filter-body-types")
    @Tag(name = "Get Filter Body Types", description = "Retrieve a list of all body types for filtering")
    public List<String> getBodyTypes() {
        return carModelService.findAllBodyTypes();
    }


}
