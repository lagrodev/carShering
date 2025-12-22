package org.example.carshering.rental.domain.repository.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.rental.domain.model.Contract;
import org.example.carshering.rental.domain.repository.ContractDomainRepository;
import org.example.carshering.rental.domain.valueobject.*;
import org.example.carshering.rental.infrastructure.persistence.entity.ContractJpaEntity;
import org.example.carshering.rental.infrastructure.persistence.mapper.ContractMapper;
import org.example.carshering.rental.infrastructure.persistence.repository.ContractRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ContractRepositoryAdapter implements ContractDomainRepository {
    private final ContractMapper mapper;
    private final ContractRepository jpaRepository;


    @Override
    public Contract save(Contract contract) {
        // домен -> джпа ентити
        ContractJpaEntity contractJpaEntity = mapper.toEntity(contract);
        log.info("Saving contract entity: {}", contractJpaEntity);
        // сайва
        ContractJpaEntity savedEntity = jpaRepository.save(contractJpaEntity);
        log.info("Saved contract entity: {}", savedEntity);
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
    public Optional<Contract> getContractByIdAndClientId(ContractId contractId, ClientId clientId) {
        return jpaRepository.findByIdAndUserId(contractId.value(), clientId.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean isCarAvailable(CarId carId, RentalPeriod period) {
        return jpaRepository.findByActiveContractsForCarInPeriod(
                period.getStartDate(),
                period.getEndDate(),
                carId.value()
        ).isEmpty();
    }

    @Override
    public List<Contract> findOverlappingContracts(CarId carId, RentalPeriod period) {
        return jpaRepository.findByActiveContractsForCarInPeriod(
                period.getStartDate(),
                period.getEndDate(),
                carId.value()
        ).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Contract> findConfirmedContractsWithStartDateBefore(LocalDateTime now) {
        return jpaRepository.findContractJpaEntitiesByState_ConfirmedAndPeriod_StartDateBefore(now).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Contract> saveAll(List<Contract> contractsToActivate) {
        List<ContractJpaEntity> saved = jpaRepository.saveAll(contractsToActivate.stream()
                .map(mapper::toEntity)
                .toList());
        return saved.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Contract> findActiveContractsWithEndDateBefore(LocalDateTime now) {
        return jpaRepository.findContractJpaEntitiesByState_ActiveAndPeriod_EndDateBefore(RentalStateType.ACTIVE.name(),now).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Page<Contract> findAllByFilter(RentalStateType status, ClientId carId, CarId clientId, String brand, String bodyType, String carClass, Pageable pageable) {
        return jpaRepository.findAllByFilter(status != null ? status.name() : null,
                        carId != null ? carId.value() : null,
                        clientId != null ? clientId.value() : null,
                        brand,
                        carClass,
                        bodyType, pageable)
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
