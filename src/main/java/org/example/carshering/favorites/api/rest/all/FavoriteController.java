package org.example.carshering.favorites.api.rest.all;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.favorites.api.facade.FavoriteFacade;
import org.example.carshering.favorites.application.service.FavoriteApplicationService;
import org.example.carshering.fleet.api.dto.responce.CarListItemResponse;
import org.example.carshering.security.ClientDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cars")
@Tag(name = "Favorites", description = "Endpoints for managing user's favorite cars")
public class FavoriteController {

    private final FavoriteFacade facade;
    private final FavoriteApplicationService favoriteService;

    @GetMapping("/favorites")
    @Operation(
            summary = "Get User Favorites",
            description = "Retrieve a paginated list of user's favorite cars"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of favorite cars retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CarListItemResponse.class)
            )
    )
    public Page<CarListItemResponse> getFavorites(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "Pagination and sorting information") Pageable pageable
    ) {
        Long userId = getCurrentUserId(auth);
        return favoriteService.getAllFavoritesByClient(new ClientId(userId), pageable).map(facade::getAllListItemForCar);
    }

    @DeleteMapping("/favorites/{carId}")
    @Operation(
            summary = "Delete Favorite",
            description = "Remove a car from user's favorites"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Favorite deleted successfully"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Favorite not found"
    )
    public ResponseEntity<Void> deleteFavorite(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "ID of the car to remove from favorites", example = "1")
            @PathVariable Long carId
    ) {
        Long userId = getCurrentUserId(auth);
        favoriteService.deleteFavorite(new ClientId(userId), new CarId(carId));
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(Authentication auth) {
        return ((ClientDetails) auth.getPrincipal()).getId();
    }

    @PostMapping("/favorites/{carId}")
    @Operation(
            summary = "Add Favorite",
            description = "Add a car to user's favorites"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Favorite added successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CarListItemResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Car not found"
    )
    public ResponseEntity<CarListItemResponse> addFavorite(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "ID of the car to add to favorites", example = "1")
            @PathVariable Long carId
    ) {
        Long userId = getCurrentUserId(auth);
        CarListItemResponse favoriteResponse = facade.getAllListItemForCar(favoriteService.addFavorite(new CarId(carId), new ClientId(userId)));
        return ResponseEntity.status(201).body(favoriteResponse);
    }

    @GetMapping("/favorites/{carId}")
    @Operation(
            summary = "Get Favorite",
            description = "Check if a specific car is in user's favorites"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Favorite found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CarListItemResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Favorite not found"
    )
    public ResponseEntity<CarListItemResponse> getFavorite(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "ID of the car to check", example = "1")
            @PathVariable Long carId
    ) {
        Long userId = getCurrentUserId(auth);
        CarListItemResponse favoriteResponse = facade.getAllListItemForCar(favoriteService.getFavoriteByClientAndCar(new CarId(carId), new ClientId(userId)));
        return favoriteResponse != null
                ? ResponseEntity.ok(favoriteResponse)
                : ResponseEntity.notFound().build();
    }

}
