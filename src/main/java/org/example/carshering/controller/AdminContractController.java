package org.example.carshering.controller;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.service.CarService;
import org.example.carshering.service.ContractService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/admin/contracts")
@RestController()
public class AdminContractController {


    ContractService contractService;

    @PatchMapping("/{contractId}/confirm")
    public ResponseEntity<?> confirmContract(@PathVariable Long contractId) {
        contractService.confirmContract(contractId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping
    public List<ContractResponse> getAllContracts() {
        return contractService.getAllContracts();
    }

    @GetMapping("/{contractId}")
    public ContractResponse getContractById(@PathVariable Long contractId) {
        return contractService.getContractById(contractId);
    }

    @PatchMapping("/{contractId}/cancel")
    public ResponseEntity<?> cancelContract(@PathVariable Long contractId) {
        contractService.cancelContract(contractId);
        return ResponseEntity.noContent().build();
    }

}
