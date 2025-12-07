package org.example.carshering.rental.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.example.carshering.rental.domain.model.Contract;
import org.example.carshering.rental.domain.repository.ContractDomainRepository;
import org.example.carshering.rental.domain.valueobject.CarId;
import org.example.carshering.rental.domain.valueobject.ClientId;
import org.example.carshering.rental.domain.valueobject.ContractId;
import org.example.carshering.rental.domain.valueobject.RentalPeriod;
import org.example.carshering.rental.infrastructure.persistence.entity.ContractJpaEntity;
import org.example.carshering.rental.infrastructure.persistence.mapper.ContractMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ContractRepositoryAdapter implements ContractDomainRepository {
    private final ContractMapper mapper;
    private final ContractRepository jpaRepository;


    @Override
    public Contract save(Contract contract) {
        // домен -> джпа ентити
        ContractJpaEntity contractJpaEntity = mapper.toEntity(contract);
        // сайва
        ContractJpaEntity savedEntity = jpaRepository.save(contractJpaEntity);
        // джпа ентити -> домен
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Contract> findById(ContractId contractId) {
        // жпа по id
        Optional<ContractJpaEntity> optionalContractJpaEntity = jpaRepository.findById(contractId.value());
        // мапинг в домен
        return optionalContractJpaEntity.map(mapper::toDomain);
    }

    @Override
    public Page<Contract> findByClientId(ClientId clientId, Pageable pageable) {

        Page<ContractJpaEntity> contractJpaEntityPage = jpaRepository.findByClientId(clientId.value(), pageable);

        return contractJpaEntityPage.map(mapper::toDomain);
    }

    @Override
    public List<Contract> findOverlappingContracts(RentalPeriod rentalPeriod, CarId carId, ContractId excludeContractId) {

        List<ContractJpaEntity> contractJpaEntities = jpaRepository.findOverlappingContracts(
                rentalPeriod.getStartDate(),
                rentalPeriod.getEndDate(),
                carId.value(),
                excludeContractId != null ? excludeContractId.value() : null
        );

        return contractJpaEntities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Contract> findActiveContractsForCarInPeriod(CarId carId, RentalPeriod period) {
        List<ContractJpaEntity> contractJpaEntities = jpaRepository.findByActiveContractsForCarInPeriod(
                period.getStartDate(),
                period.getEndDate(),
                carId.value()
        );

        return contractJpaEntities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Contract> findByIdAndClientId(ContractId contractId, ClientId clientId) {
        return jpaRepository.findByIdAndUserId(contractId.value(), clientId.value())
                .map(mapper::toDomain);
    }

//    @Override
//    public void deleteById(ContractId contractId) {
//
//        Contract contract = findById(contractId).orElseThrow(
//                () -> new NotFoundException("Contract not found with id: " + contractId.value()));
//
//        contract.cancel();
//
//        save(contract);
//    }


}
