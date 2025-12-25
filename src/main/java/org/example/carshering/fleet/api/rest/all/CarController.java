package org.example.carshering.fleet.api.rest.all;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.fleet.api.dto.request.CarFilterRequest;
import org.example.carshering.fleet.api.dto.responce.CarDetailResponse;
import org.example.carshering.fleet.api.dto.responce.CarListItemResponse;
import org.example.carshering.fleet.api.dto.responce.MinMaxCellForFilters;
import org.example.carshering.fleet.api.facade.CarFacade;
import org.example.carshering.fleet.application.dto.response.BrandDto;
import org.example.carshering.fleet.application.dto.response.CarClassDto;
import org.example.carshering.fleet.application.dto.response.ModelNameDto;
import org.example.carshering.fleet.application.service.BrandApplicationService;
import org.example.carshering.fleet.application.service.CarApplicationService;
import org.example.carshering.fleet.application.service.CarClassApplicationService;
import org.example.carshering.fleet.application.service.CarModelApplicationService;
import org.example.carshering.fleet.application.service.ModelNameApplicationService;
import org.example.carshering.security.ClientDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@Tag(name = "Car Catalogue", description = "Endpoints for browsing available cars and filters")
@RequiredArgsConstructor
@RequestMapping("api/car")
public class CarController {

    // Application Services (Fleet context)
    private final CarApplicationService carApplicationService;
    private final CarModelApplicationService carModelApplicationService;
    private final BrandApplicationService brandApplicationService;
    private final ModelNameApplicationService modelNameApplicationService;
    private final CarClassApplicationService carClassApplicationService;

    // Facade для сборки Response с данными из других контекстов
    private final CarFacade carFacade;

    public static CarFilterRequest createFilter(String brand,
                                                String model,
                                                Integer minYear,
                                                Integer maxYear,
                                                String bodyType,
                                                String carClass,
                                                String carState,
                                                LocalDateTime dateStart,
                                                LocalDateTime dateEnd,
                                                BigDecimal minCell,
                                                BigDecimal maxCell
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
            description = "Retrieve a paginated list of available cars with optional filtering by brand, model, year, body type, class, dates, and price range"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of available cars retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)
            )
    )
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
            @RequestParam(value = "date_start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateStart,
            @Parameter(description = "End date for car availability", example = "2025-01-31")
            @RequestParam(value = "date_end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateEnd,
            @Parameter(description = "Minimum price per day", example = "1000")
            @RequestParam(value = "min_cell", required = false) BigDecimal minCell,
            @Parameter(description = "Maximum price per day", example = "5000")
            @RequestParam(value = "max_cell", required = false) BigDecimal maxCell,
            @Parameter(description = "Pagination and sorting information")
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) Authentication auth
    ) {
        // Создаем фильтр (только доступные автомобили)
        var filter = createFilter(brand, model, minYear, maxYear, bodyType, carClass, "AVAILABLE", dateStart, dateEnd, minCell, maxCell);

        // Получаем данные из Application Service
        var carsPage = carApplicationService.getAllCars(pageable, filter);

        // Получаем userId для favorite
        Long userId = auth != null ? ((ClientDetails) auth.getPrincipal()).getId() : null;

        if (userId != null) {
            log.info("User '{}' is browsing the car catalogue with filters: brand='{}', model='{}', minYear='{}', maxYear='{}', bodyType='{}', carClass='{}', dateStart='{}', dateEnd='{}', minCell='{}', maxCell='{}'.",
                    auth.getName(), brand, model, minYear, maxYear, bodyType, carClass, dateStart, dateEnd, minCell, maxCell);
        }

        // Фасад собирает финальный Response с favorite
        return carFacade.toListItemResponsePage(carsPage, userId);
    }

    @GetMapping("/filters/min-max-cell")
    @Operation(
            summary = "Get Min and Max Cell for Filters",
            description = "Retrieve the minimum and maximum rental prices per day for available cars based on current filter parameters"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Minimum and maximum rental prices retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MinMaxCellForFilters.class)
            )
    )
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
            @RequestParam(value = "date_start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateStart,
            @Parameter(description = "End date for car availability", example = "2025-01-31")
            @RequestParam(value = "date_end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateEnd
    ) {
        var filter = createFilter(brand, model, minYear, maxYear, bodyType, carClass, "AVAILABLE", dateStart, dateEnd, null, null);
        return carApplicationService.getMinMaxCell(filter);
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
    @ApiResponse(
            responseCode = "404",
            description = "Car not found or not available"
    )
    public CarDetailResponse findValidCar(
            @Parameter(description = "ID of the car to retrieve", example = "1", required = true)
            @PathVariable Long carId,
            @Parameter(hidden = true) Authentication auth
    ) {
        // Получаем данные из Application Service
        var carDto = carApplicationService.getValidCarById(carId, false);

        // Получаем userId для favorite
        Long userId = auth != null ? ((ClientDetails) auth.getPrincipal()).getId() : null;

        // Фасад собирает финальный Response с favorite
        return carFacade.toDetailResponse(carDto, userId);
    }

    @GetMapping("/filters/brands")
    @Operation(
            summary = "Get Filter Brands",
            description = "Retrieve a list of all available car brands for filtering"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of brands retrieved successfully"
    )
    public List<String> getBrands() {
        return brandApplicationService.getAllBrands()
                .stream()
                .map(BrandDto::name)
                .toList();
    }

    @GetMapping("/filters/models")
    @Operation(
            summary = "Get Filter Models",
            description = "Retrieve a list of all available model names for filtering"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of model names retrieved successfully"
    )
    public List<String> getModels() {
        return modelNameApplicationService.getAllCarClass()
                .stream()
                .map(ModelNameDto::name)
                .toList();
    }

    @GetMapping("/filters/classes")
    @Operation(
            summary = "Get Filter Classes",
            description = "Retrieve a list of all available car classes for filtering"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of car classes retrieved successfully"
    )
    public List<String> getClasses() {
        return carClassApplicationService.getAllCarClass()
                .stream()
                .map(CarClassDto::name)
                .toList();
    }

    @GetMapping("/filters/body-types")
    @Operation(
            summary = "Get Filter Body Types",
            description = "Retrieve a list of all available body types for filtering"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of body types retrieved successfully"
    )
    public List<String> getBodyTypes() {
        return carModelApplicationService.findAllBodyTypes();
    }


}
