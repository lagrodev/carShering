package org.example.carshering.rental.domain.repository;

import org.example.carshering.rental.domain.model.Contract;
import org.example.carshering.rental.domain.valueobject.CarId;
import org.example.carshering.rental.domain.valueobject.ClientId;
import org.example.carshering.rental.domain.valueobject.ContractId;
import org.example.carshering.rental.domain.valueobject.RentalPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ContractDomainRepository {
    // Save or update a contract
    Contract save(Contract contract);

    // Find a contract by its ID
    Optional<Contract> findById(ContractId contractId);

    // Find contracts by client ID with pagination
    Page<Contract> findByClientId(ClientId clientId, Pageable pageable);

    // Find overlapping contracts for a given rental period and car, excluding a specific contract ID
    List<Contract> findOverlappingContracts(RentalPeriod rentalPeriod,
                                            CarId carId, ContractId excludeContractId);

    // Find active contracts for a specific car within a given rental period
    List<Contract> findActiveContractsForCarInPeriod(CarId carId, RentalPeriod period);

    //    // Delete a contract by its ID
//    void deleteById(ContractId contractId);
    Optional<Contract> findByIdAndClientId(ContractId contractId, ClientId clientId);


}
