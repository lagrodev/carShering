package org.example.carshering.rest.all;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.response.RideStats;
import org.example.carshering.dto.response.UserStats;
import org.example.carshering.security.ClientDetails;
import org.example.carshering.service.interfaces.AnalysisService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@Tag(name = "User Analytics", description = "Endpoints for user statistics and analytics")
@RequiredArgsConstructor
public class AnalysisController {


    private final AnalysisService analysisService;

    // Обзорная статистика пользователя
    @GetMapping("/overview/client")
    @Operation(
            summary = "Get User Overview Statistics",
            description = "Retrieve comprehensive statistics for the authenticated user including favorite car, total rides, spending, and usage patterns",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User statistics retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserStats.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User not authenticated",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> getOverviewStats(
            @Parameter(hidden = true, description = "Authentication token of the current user") Authentication auth
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(analysisService.getOverviewStats(getCurrentUserId(auth)));
    }

    private Long getCurrentUserId(Authentication auth) {
        return ((ClientDetails) auth.getPrincipal()).getId();
    }

    // Статистика по поездкам пользователя - за последний месяц
    @GetMapping("/ridesLastMonth")
    @Operation(
            summary = "Get User Ride Statistics for Last Month",
            description = "Retrieve detailed ride statistics for all users in the last month including total rides, average duration, and distance traveled. Supports pagination.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User ride statistics retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RideStats.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User not authenticated",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> getRideStats(
            @Parameter(description = "Pagination parameters (page, size, sort)", example = "page=0&size=10&sort=ridesCount,desc") Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(analysisService.getRidesLastMonth(pageable));
    }

    // Статистика по поездкам пользователя - за все время
    @GetMapping("/rideStats")
    @Operation(
            summary = "Get User Ride Statistics Summary (All Time)",
            description = "Retrieve a comprehensive summary of ride statistics for all users across all time including total rides, average duration, and distance traveled. Supports pagination for leaderboard display.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User ride statistics summary retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RideStats.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User not authenticated",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> getRideStatsSummary(
            @Parameter(description = "Pagination parameters (page, size, sort)", example = "page=0&size=10&sort=ridesCount,desc") Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(analysisService.getRideAllTime(pageable));
    }





}
