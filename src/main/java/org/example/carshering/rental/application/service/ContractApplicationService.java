package org.example.carshering.rental.application.service;

import org.example.carshering.rental.api.dto.request.FilterContractRequest;
import org.example.carshering.rental.api.dto.request.CreateContractRequest;
import org.example.carshering.rental.api.dto.request.UpdateContractRequest;
import org.example.carshering.rental.application.dto.response.ContractDto;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.rental.domain.valueobject.ContractId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public interface ContractApplicationService {

    // User methods
    @Transactional
    ContractDto createContract(Long userId, CreateContractRequest request, BigDecimal money);

    @Transactional
    ContractDto updateContract(Long userId, Long contractId, UpdateContractRequest request);

    ContractDto getUserContract(Long contractId, Long userId);

    @Transactional
    void cancelContractByUser(ClientId clientId, ContractId contractId);



    Page<ContractDto> getAllContractsByClientId(ClientId clientId, Pageable pageable);

    // Admin methods
    @Transactional
    void cancelContractByAdmin(ContractId contractId);

    @Transactional
    void confirmContract(ContractId contractId);

    Page<ContractDto> getAllContractsByAdmin(Pageable pageable, FilterContractRequest filter);

    ContractDto getContractByIdForAdmin(Long contractId);


    // Scheduler
    @Transactional
    void activateConfirmedContracts();

    @Transactional
    void completeActiveContracts();

}
