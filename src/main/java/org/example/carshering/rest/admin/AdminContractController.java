package org.example.carshering.rest.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.FilterContractRequest;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.service.ContractService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Tag(name = "Admin Contract Management", description = "Endpoints for admin contract management")
@RequestMapping("/api/admin/contracts")
@RestController()
public class AdminContractController {


    private final ContractService contractService;

    @PatchMapping("/{contractId}/confirm")
    @Operation(
            summary = "Confirm Contract",
            description = "Confirm a contract by its ID"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Contract confirmed successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ContractResponse.class)
            )
    )
    @Tag(name = "confirm-contract")
    @Tag(name = "Confirm Contract", description = "Confirm a contract by its ID")
    public ResponseEntity<?> confirmContract(
            @Parameter(description = "ID of the contract to confirm", example = "1")
            @PathVariable Long contractId
    ) {
        ContractResponse contractResponse = contractService.confirmContract(contractId);
        return ResponseEntity.ok().body(contractResponse);
    }    // todo логику подтверждения контракта
    // todo сортировка, мю сделать в таблице индексацию по подтверждению, фильтрация,

    @GetMapping
    @Operation(
            summary = "Get All Contracts",
            description = "Retrieve a paginated list of contracts with optional filtering"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of contracts retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ContractResponse.class)
            )
    )
    @Tag(name = "get-all-contracts")
    @Tag(name = "Get All Contracts", description = "Retrieve a paginated list of contracts with optional filtering")
    public Page<ContractResponse> getAllContracts(
            @Parameter(description = "Filter by contract status", example = "ACTIVE")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by user ID", example = "1")
            @RequestParam(required = false) Long idUser,
            @Parameter(description = "Filter by car ID", example = "1")
            @RequestParam(required = false) Long idCar,
            @Parameter(description = "Filter by car brand", example = "Toyota")
            @RequestParam(value = "brand", required = false) String brand,
            @Parameter(description = "Filter by body type", example = "Sedan")
            @RequestParam(value = "body_type", required = false) String bodyType,
            @Parameter(description = "Filter by car class", example = "Business")
            @RequestParam(value = "car_class", required = false) String carClass,
            @Parameter(description = "Pagination and sorting information")
            @PageableDefault(size = 20, sort = "id") Pageable pageable

    ) {
        var filter = new FilterContractRequest(status, idUser, idCar, brand, bodyType, carClass);
        return contractService.getAllContracts(pageable, filter);
    }

    @GetMapping("/{contractId}")
    @Operation(
            summary = "Get Contract by ID",
            description = "Retrieve detailed information about a specific contract by its ID"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Contract details retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ContractResponse.class)
            )
    )
    @Tag(name = "get-contract-by-id")
    @Tag(name = "Get Contract by ID", description = "Retrieve detailed information about a specific contract by its ID")
    public ContractResponse getContractById(
            @Parameter(description = "ID of the contract to retrieve", example = "1")
            @PathVariable Long contractId
    ) {
        return contractService.getContractById(contractId);
    }

    @DeleteMapping("/{contractId}/cancel")
    @Operation(
            summary = "Cancel Contract",
            description = "Cancel a contract by its ID"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Contract cancelled successfully"
    )
    @Tag(name = "cancel-contract")
    @Tag(name = "Cancel Contract", description = "Cancel a contract by its ID")
    public ResponseEntity<?> cancelContract(
            @Parameter(description = "ID of the contract to cancel", example = "1")
            @PathVariable Long contractId
    ) {
        contractService.cancelContractByAdmin(contractId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/contracts/{id}/confirm-cancellation")
    @Operation(
            summary = "Confirm Cancellation",
            description = "Confirm the cancellation of a contract by admin"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Cancellation confirmed successfully"
    )
    @Tag(name = "confirm-cancellation")
    @Tag(name = "Confirm Cancellation", description = "Confirm the cancellation of a contract by admin")
    public ResponseEntity<Void> confirmCancellation(
            @Parameter(description = "ID of the contract to confirm cancellation", example = "1")
            @PathVariable Long id
    ) {
        contractService.confirmCancellationByAdmin(id);
        return ResponseEntity.noContent().build();
    }

}
