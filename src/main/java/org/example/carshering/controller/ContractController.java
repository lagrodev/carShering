package org.example.carshering.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.CreateContractRequest;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.dto.response.DocumentResponse;
import org.example.carshering.security.ClientDetails;
import org.example.carshering.service.ContractService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.security.core.Authentication;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    public ResponseEntity<?> createContract(
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
    public List<ContractResponse> getAllContracts(Authentication auth) {
        Long userId = getCurrentUserId(auth);
        return contractService.getAllContracts(userId);
    }

    @GetMapping("/{contractId}")
    public ContractResponse getContract(
            @PathVariable Long contractId,
            Authentication auth
    ) {
        Long userId = getCurrentUserId(auth);
        return contractService.findContract(contractId, userId);
    }

    @DeleteMapping("/{contractId}/cancel")
    public ResponseEntity<?> cancelContract(
            @PathVariable Long contractId,
            Authentication auth
    ) {
        Long userId = getCurrentUserId(auth);
        contractService.cancelContract(contractId, userId);
        return ResponseEntity.noContent().build();
    } // todo логику отмены
    // todo изменение контракта


    private Long getCurrentUserId(Authentication auth) {
        return ((ClientDetails) auth.getPrincipal()).getId();
    }
}
