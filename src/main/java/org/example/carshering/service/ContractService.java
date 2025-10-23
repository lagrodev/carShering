package org.example.carshering.service;

import jakarta.validation.Valid;
import org.example.carshering.dto.request.CreateContractRequest;
import org.example.carshering.dto.request.FilterContractRequest;
import org.example.carshering.dto.request.UpdateContractRequest;
import org.example.carshering.dto.response.ContractResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContractService {

    ContractResponse createContract(Long userId, CreateContractRequest request);

    void cancelContract(Long userId, Long contractId);

    void cancelContractByAdmin(Long contractId);

    void confirmCancellationByAdmin(Long contractId);

    ContractResponse findContract(Long contractId, Long userId);

    Page<ContractResponse> getAllClientContracts(Pageable pageable, Long userId);

    Page<ContractResponse> getAllContracts(Pageable pageable, FilterContractRequest filter);

    ContractResponse getContractById(Long contractId);

    void confirmContract(Long contractId);

    ContractResponse updateContract(Long userId, Long contractId, UpdateContractRequest request);
}
