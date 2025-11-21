package org.example.carshering.service.interfaces;

import org.example.carshering.dto.request.create.CreateContractRequest;
import org.example.carshering.dto.request.FilterContractRequest;
import org.example.carshering.dto.request.update.UpdateContractRequest;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContractService {

    ContractResponse createContract(Long userId, CreateContractRequest request);

    void cancelContract(Long userId, Long contractId);

    void cancelContractByAdmin(Long contractId);

    void confirmCancellationByAdmin(Long contractId);

    ContractResponse findContract(Long contractId, Long userId);

    Page<ContractResponse> getAllClientContracts(Pageable pageable, Long userId);

    Page<ContractResponse> getAllContracts(Pageable pageable, FilterContractRequest filter);

    ContractResponse getContractById(Long contractId);

    ContractResponse confirmContract(Long contractId);

    ContractResponse updateContract(Long userId, Long contractId, UpdateContractRequest request);

    void checkAndAllActiveContractsByClient(Client client);
}
