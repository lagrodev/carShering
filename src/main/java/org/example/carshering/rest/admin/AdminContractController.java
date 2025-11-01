package org.example.carshering.rest.admin;

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
@RequestMapping("/api/admin/contracts")
@RestController()
public class AdminContractController {


    private final ContractService contractService;

    @PatchMapping("/{contractId}/confirm")
    public ResponseEntity<?> confirmContract(
            @PathVariable Long contractId
    ) {
        contractService.confirmContract(contractId);
        return ResponseEntity.noContent().build();
    }    // todo логику подтверждения контракта
    // todo сортировка, мю сделать в таблице индексацию по подтверждению, фильтрация,

    @GetMapping
    public Page<ContractResponse> getAllContracts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long idUser,
            @RequestParam(required = false) Long idCar,
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "body_type", required = false) String bodyType,
            @RequestParam(value = "car_class", required = false) String carClass,
            @PageableDefault(size = 20, sort = "id") Pageable pageable

    ) {
        var filter = new FilterContractRequest(status, idUser, idCar, brand, bodyType, carClass);
        return contractService.getAllContracts(pageable, filter);
    }

    @GetMapping("/{contractId}")
    public ContractResponse getContractById(@PathVariable Long contractId) {
        return contractService.getContractById(contractId);
    }

    @DeleteMapping("/{contractId}/cancel")
    public ResponseEntity<?> cancelContract(@PathVariable Long contractId) {
        contractService.cancelContractByAdmin(contractId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/contracts/{id}/confirm-cancellation")
    public ResponseEntity<Void> confirmCancellation(@PathVariable Long id) {
        contractService.confirmCancellationByAdmin(id);
        return ResponseEntity.noContent().build();
    }

}
