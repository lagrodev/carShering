package org.example.carshering.rental.api.rest.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.FilterContractRequest;
import org.example.carshering.rental.api.dto.response.ContractResponse;
import org.example.carshering.rental.api.facade.ContractResponseFacade;
import org.example.carshering.rental.application.service.ContractApplicationService;
import org.example.carshering.rental.domain.valueobject.ContractId;
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


    private final ContractApplicationService contractService;
    private final ContractResponseFacade responseFacade;

    @PatchMapping("/{contractId}/confirm")
    @Operation(
            summary = "Confirm Contract",
            description = "Confirm a contract by its ID (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Contract confirmed successfully",
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
            responseCode = "400",
            description = "Contract cannot be confirmed"
    )
    public ResponseEntity<ContractResponse> confirmContract(
            @Parameter(description = "ID of the contract to confirm", example = "1", required = true)
            @PathVariable Long contractId
    ) {
        contractService.confirmContract(new ContractId(contractId));

        // Получаем обновлённый контракт и собираем Response
        var contractDto = contractService.getContractByIdForAdmin(contractId);
        ContractResponse contractResponse = responseFacade.getContractResponse(contractDto);

        return ResponseEntity.ok().body(contractResponse);
    }

    @GetMapping
    @Operation(
            summary = "Get All Contracts",
            description = "Retrieve a paginated list of all contracts with optional filtering by status, user, car, brand, body type, and class (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of contracts retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)
            )
    )
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

        // Получаем Page<ContractDto> из Application Service
        var contractDtos = contractService.getAllContractsByAdmin(pageable, filter);

        // Facade собирает Response с batch loading (решение N+1)
        return responseFacade.getContractsResponses(contractDtos);
    }

    @GetMapping("/{contractId}")
    @Operation(
            summary = "Get Contract by ID",
            description = "Retrieve detailed information about a specific contract by its ID (admin access)"
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
    public ContractResponse getContractById(
            @Parameter(description = "ID of the contract to retrieve", example = "1", required = true)
            @PathVariable Long contractId
    ) {
        // Application Service возвращает ContractDto
        var contractDto = contractService.getContractByIdForAdmin(contractId);

        // Facade собирает полный Response
        return responseFacade.getContractResponse(contractDto);
    }

    @DeleteMapping("/{contractId}/cancel")
    @Operation(
            summary = "Cancel Contract",
            description = "Cancel a contract by its ID (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Contract cancelled successfully"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Contract not found"
    )
    public ResponseEntity<?> cancelContract(
            @Parameter(description = "ID of the contract to cancel", example = "1", required = true)
            @PathVariable Long contractId
    ) {
        contractService.cancelContractByAdmin(new ContractId(contractId));
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
    @ApiResponse(
            responseCode = "404",
            description = "Contract not found"
    )
    public ResponseEntity<Void> confirmCancellation(
            @Parameter(description = "ID of the contract to confirm cancellation", example = "1", required = true)
            @PathVariable Long id
    ) {
        // TODO: добавить метод confirmCancellationByAdmin в ContractApplicationService
        contractService.cancelContractByAdmin(new ContractId(id));

        return ResponseEntity.noContent().build();
    }

}
