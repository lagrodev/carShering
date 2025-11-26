package org.example.carshering.rest.all;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.create.CreateContractRequest;
import org.example.carshering.dto.request.update.UpdateContractRequest;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.security.ClientDetails;
import org.example.carshering.service.interfaces.ContractService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.security.core.Authentication;
import java.net.URI;

@RestController
@Tag(name = "Contract Management", description = "Endpoints for user contract management")
@RequestMapping("api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    @Operation(
            summary = "Create Contract",
            description = "Create a new rental contract for the authenticated user"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Contract created successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ContractResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request data or car not available for specified dates"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Car not found"
    )
    public ResponseEntity<?> createContract(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Contract details to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateContractRequest.class)
                    )
            )
            @Valid @RequestBody CreateContractRequest request,
            Authentication auth
    ) {
        Long userId = getCurrentUserId(auth);
        ContractResponse response = contractService.createContract(userId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Get User Contracts",
            description = "Retrieve all rental contracts for the authenticated user with pagination"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of user contracts retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)
            )
    )
    public Page<ContractResponse> getAllContracts(
            Authentication auth,
            @Parameter(description = "Pagination and sorting information")
            Pageable pageable
    ) {
        Long userId = getCurrentUserId(auth);
        return contractService.getAllClientContracts(pageable, userId);
    }

    @GetMapping("/{contractId}")
    @Operation(
            summary = "Get Contract",
            description = "Retrieve detailed information about a specific contract by its ID (only for contract owner)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Contract details retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ContractResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Contract not found"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied - not contract owner"
    )
    public ContractResponse getContract(
            @Parameter(description = "ID of the contract to retrieve", example = "1", required = true)
            @PathVariable Long contractId,
            Authentication auth
    ) {
        Long userId = getCurrentUserId(auth);
        return contractService.findContract(contractId, userId);
    }

    @DeleteMapping("/{contractId}/cancel")
    @Operation(
            summary = "Cancel User Contract",
            description = "Cancel a rental contract by the authenticated user (only contract owner can cancel)"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Contract cancelled successfully"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Contract not found"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied - not contract owner"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Contract cannot be cancelled"
    )
    public ResponseEntity<?> cancelContract(
            @Parameter(description = "ID of the contract to cancel", example = "1", required = true)
            @PathVariable Long contractId,
            Authentication auth
    ) {
        Long userId = getCurrentUserId(auth);
        contractService.cancelContract(userId, contractId );
        return ResponseEntity.noContent().build();
    } // todo логику отмены
    // todo изменение контракта

    @PatchMapping("/{contractId}")
    @Operation(
            summary = "Update Contract",
            description = "Update the rental dates of an existing contract (only contract owner can update)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Contract updated successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ContractResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Contract not found"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied - not contract owner"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request data or dates conflict with existing bookings"
    )
    public ResponseEntity<?> updateContract(
            @Parameter(description = "ID of the contract to update", example = "1", required = true)
            @PathVariable Long contractId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated contract details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateContractRequest.class)
                    )
            )
            @Valid @RequestBody UpdateContractRequest request,
            Authentication auth
    ){
        Long userId = getCurrentUserId(auth);
        ContractResponse response = contractService.updateContract(userId, contractId, request);
        return ResponseEntity.ok(response);
    }


    private Long getCurrentUserId(Authentication auth) {
        return ((ClientDetails) auth.getPrincipal()).getId();
    }
}
