package org.example.carshering.rest.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.FilterUserRequest;
import org.example.carshering.dto.request.RoleRequested;
import org.example.carshering.dto.response.AllUserResponse;
import org.example.carshering.dto.response.ShortUserResponse;
import org.example.carshering.service.interfaces.ClientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Admin Client Management", description = "Endpoints for admin client management")
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminClientController {

    private final ClientService clientService;

    // пользователи

    @GetMapping("/users/{userId}")
    @Operation(
            summary = "Get User by ID",
            description = "Retrieve detailed information about a specific user by their ID (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "User details retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AllUserResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "User not found"
    )
    public AllUserResponse getUser(
            @Parameter(description = "ID of the user to retrieve", example = "1", required = true)
            @PathVariable Long userId
    ) {
        return clientService.findAllUser(userId);
    }

    @GetMapping("/users")
    @Operation(
            summary = "Get All Users",
            description = "Retrieve a paginated list of users with optional filtering by ban status and role (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of users retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)
            )
    )
    public Page<ShortUserResponse> getAllUsers(
            @Parameter(description = "Filter by banned status", example = "false")
            @RequestParam(value = "banned", required = false) Boolean banned,
            @Parameter(description = "Filter by role name", example = "USER")
            @RequestParam(value = "roleName", required = false) String roleName,
            @Parameter(description = "Pagination and sorting information")
            @PageableDefault(size = 20, sort = "banned") Pageable pageable
    ) {
        var filter = new FilterUserRequest(
                banned,
                roleName
        );
        return clientService.filterUsers(filter, pageable);
    }


// баны + роли, мб еще удаление сделать? хотя, это тоже самое, что и бан пока
    // todo мб, удаление тоже?
    @PatchMapping("/users/{userId}/ban")
    @Operation(
            summary = "Ban User",
            description = "Ban a user by their ID (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "User banned successfully"
    )
    @ApiResponse(
            responseCode = "404",
            description = "User not found"
    )
    public ResponseEntity<?> banUser(
            @Parameter(description = "ID of the user to ban", example = "1", required = true)
            @PathVariable Long userId
    ) {
        var client = clientService.banUser(userId);
        return ResponseEntity.ok().body(client);
    }

    @PatchMapping("/users/{userId}/unban")
    @Operation(
            summary = "Unban User",
            description = "Unban a user by their ID (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "User unbanned successfully"
    )
    @ApiResponse(
            responseCode = "404",
            description = "User not found"
    )
    public ResponseEntity<?> unbanUser(
            @Parameter(description = "ID of the user to unban", example = "1", required = true)
            @PathVariable Long userId
    ) {
        var client = clientService.unbanUser(userId);
        return ResponseEntity.ok().body(client);
    }

    @PatchMapping("/users/{userId}/updateRole")
    @Operation(
            summary = "Update User Role",
            description = "Update the role of a user by their ID (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "User role updated successfully"
    )
    @ApiResponse(
            responseCode = "404",
            description = "User not found"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid role name"
    )
    public ResponseEntity<?> updateRole(
            @Parameter(description = "ID of the user to update role", example = "1", required = true)
            @PathVariable Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New role for the user",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RoleRequested.class)
                    )
            )
            @Valid @RequestBody RoleRequested roleRequested) {
        var client = clientService.updateRole(userId, roleRequested.RoleName());
        return ResponseEntity.ok().body(client);
    }


}