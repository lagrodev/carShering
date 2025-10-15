package org.example.carshering.service;

import org.example.carshering.dto.request.CreateContractRequest;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.entity.Contract;

import java.util.List;

public interface ContractService {

    ContractResponse createContract(Long userId, CreateContractRequest request);

    void cancelContract(Long userId, Long contractId);
    void cancelContract(Long contractId);
    ContractResponse findContract(Long contractId, Long userId);
    List<ContractResponse> getAllContracts(Long userId);
    List<ContractResponse> getAllContracts();

    ContractResponse getContractById(Long contractId);

    void confirmContract(Long contractId);
}
