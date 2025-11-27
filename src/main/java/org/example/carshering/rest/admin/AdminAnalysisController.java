package org.example.carshering.rest.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.response.AdminOverview;
import org.example.carshering.dto.response.ContractDetailResponse;
import org.example.carshering.dto.response.DailyRevenueResponse;
import org.example.carshering.service.interfaces.AnalysisService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/stats")
@Tag(name = "Admin Analytics", description = "Administrative endpoints for system statistics and analytics")
public class AdminAnalysisController {
    // Статистические эндпоинты будут добавлены здесь
    /**
     * TODO:
     * 1. Эндпоинт для получения общей статистики по пользователям (количество зарегистрированных пользователей, активные пользователи и т.д.)
     * 2. Эндпоинт для получения статистики по арендам (количество аренд, средняя продолжительность аренды, популярные автомобили и т.д.)
     * 3. Эндпоинт для получения финансовой статистики (общий доход, средний доход на пользователя и т.д.)
     * 5. Документация Swagger для всех эндпоинтов статистики
     * GET /api/stats/overview
     * GET /api/stats/rides?from=2025-11-01&to=2025-11-25
     * GET /api/stats/revenue?period=month
     * GET /api/stats/fleet/usage
     * GET /api/stats/users/active
     * GET /api/stats/cars/popular
     *
     */
    private final AnalysisService analysisService;

    @GetMapping("/overview")
    @Operation(
            summary = "Get Admin Overview Statistics",
            description = "Retrieve comprehensive overview statistics for administrators including total users, cars, contracts, revenue, and system metrics",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Admin overview statistics retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AdminOverview.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User not authenticated",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access denied - admin role required",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> getOverviewStats() {
        return ResponseEntity.status(HttpStatus.OK).body(analysisService.getOverviewStatsAdmin());
    }

    @GetMapping("/daily-revenue")
    @Operation(
            summary = "Get Daily Revenue Statistics",
            description = "Retrieve daily revenue data for a specified date range. Returns revenue aggregated by day.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Daily revenue statistics retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = DailyRevenueResponse.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid date format or date range",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User not authenticated",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access denied - admin role required",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<List<DailyRevenueResponse>> getDailyRevenue(
            @Parameter(description = "Start date of the range (inclusive)", example = "2025-11-01", required = true)
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate from,

            @Parameter(description = "End date of the range (inclusive)", example = "2025-11-30", required = true)
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate to
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(analysisService.getDailyRevenueBetween(from, to));
    }

    @GetMapping("/contracts-by-day")
    @Operation(
            summary = "Get Contracts by Day",
            description = "Retrieve all contract details for a specific day including client information, car details, costs, and duration.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Contracts retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ContractDetailResponse.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid date format",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User not authenticated",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access denied - admin role required",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<List<ContractDetailResponse>> getContractsByDay(
            @Parameter(description = "Date to retrieve contracts for", example = "2025-11-27", required = true)
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate date
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(analysisService.getContractDetailsForDay(date));
    }

    @GetMapping("/cars/top")
    @Operation(
            summary = "Get Top Cars by Profit",
            description = "Retrieve top performing cars ranked by profit within a specified date range. " +
                    "Includes detailed analytics such as total mileage, average earnings, unique users count, " +
                    "rental time, and total profit for the period.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Top cars statistics retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = org.springframework.data.domain.Page.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid date format or pagination parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User not authenticated",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access denied - admin role required",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> getTopCarsByProfit(
            @Parameter(description = "Start date of the analysis period (inclusive)", example = "2025-11-01", required = true)
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate from,

            @Parameter(description = "End date of the analysis period (inclusive)", example = "2025-11-30", required = true)
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate to,

            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return ResponseEntity.status(HttpStatus.OK).body(analysisService.getTopCarsByProfit(from, to, pageable));
    }

    @GetMapping("/cars/analytics")
    @Operation(
            summary = "Get All Cars Analytics",
            description = "Retrieve comprehensive analytics for all cars within a specified date range. " +
                    "Returns paginated results with detailed statistics including mileage, earnings, " +
                    "user count, rental duration, and profit. Sorted by car ID for consistent ordering.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cars analytics retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = org.springframework.data.domain.Page.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid date format or pagination parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User not authenticated",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access denied - admin role required",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> getAllCarsAnalytics(
            @Parameter(description = "Start date of the analysis period (inclusive)", example = "2025-11-01", required = true)
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate from,

            @Parameter(description = "End date of the analysis period (inclusive)", example = "2025-11-30", required = true)
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate to,

            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return ResponseEntity.status(HttpStatus.OK).body(analysisService.getAllCarsAnalytics(from, to, pageable));
    }

}
