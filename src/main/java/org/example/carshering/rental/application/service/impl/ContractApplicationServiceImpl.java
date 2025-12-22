package org.example.carshering.rental.application.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.common.domain.valueobject.Money;
import org.example.carshering.common.exceptions.custom.*;
import org.example.carshering.dto.request.FilterContractRequest;
import org.example.carshering.rental.api.dto.request.CreateContractRequest;
import org.example.carshering.rental.api.dto.request.UpdateContractRequest;
import org.example.carshering.rental.application.dto.response.ContractDto;
import org.example.carshering.rental.application.mapper.ContractMapperForApplication;
import org.example.carshering.rental.application.port.IdentityPort;
import org.example.carshering.rental.application.service.ContractApplicationService;
import org.example.carshering.rental.domain.model.Contract;
import org.example.carshering.rental.domain.repository.ContractDomainRepository;
import org.example.carshering.rental.domain.service.RentalDomainService;
import org.example.carshering.rental.domain.valueobject.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContractApplicationServiceImpl implements ContractApplicationService {
    private final ContractDomainRepository contractRepository;
    private final RentalDomainService rentalDomainService;
    private final ContractMapperForApplication contractMapper;
    private final IdentityPort identityPort;

    @Transactional
    @Override
    public void cancelContractByUser(ClientId clientId, ContractId contractId) {
        Contract contract = contractRepository.getContractByIdAndClientId(contractId, clientId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        if (!contract.getClientId().equals(clientId)) {
            throw new UnauthorizedContractAccessException("Contract not found for this client");
        }

        boolean withoutFee = rentalDomainService.canCanceledWithoutFee(contract);

        if (withoutFee) {
            contract.cancel();
        } else {
            // Пользователь не может отменить с штрафом - запрос админу
            contract.requestCancellation();
        }

        contractRepository.save(contract);
    }

    @Transactional
    @Override
    public void cancelContractByAdmin(ContractId contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        contract.cancel();

        contractRepository.save(contract);
    }

    @Transactional
    @Override
    public void confirmContract(ContractId contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        contract.confirm();

        contractRepository.save(contract);
    }

    @Override
    @Transactional
    public void activateConfirmedContracts() {
        LocalDateTime now = LocalDateTime.now();

        // Получаем Domain модели
        List<Contract> contractsToActivate = contractRepository
                .findConfirmedContractsWithStartDateBefore(now);

        if (contractsToActivate.isEmpty()) {
            return;
        }

        // Работаем с Domain моделями
        for (Contract contract : contractsToActivate) {
            contract.activate(); // Бизнес-логика в агрегате // сделать, чтобы он ивент добавлял, мб потом чтобы уведомления слать
        }

        // Сохраняем через репозиторий
        contractRepository.saveAll(contractsToActivate);
    }

    @Transactional
    @Override
    public void completeActiveContracts() {
        LocalDateTime now = LocalDateTime.now();

        List<Contract> contractsToComplete = contractRepository
                .findActiveContractsWithEndDateBefore(now);

        if (contractsToComplete.isEmpty()) {
            return;
        }

        for (Contract contract : contractsToComplete) {
            contract.complete(); // Бизнес-логика в агрегате
        }

        contractRepository.saveAll(contractsToComplete);
    }

    @Transactional
    @Override
    public ContractDto updateContract(Long userId, Long contractId, UpdateContractRequest request) {

        Contract contract = contractRepository.getContractByIdAndClientId(new ContractId(contractId), new ClientId(userId))
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        RentalPeriod newPeriod = RentalPeriod.of(request.dataStart(), request.dataEnd());


        if (!rentalDomainService.isCarAvailableForRental(
                contract.getCarId(),
                newPeriod,
                contract.getId())) {
            throw new CarUnavailableOnDatesException(
                    "Car is not available for the new dates"
            );
        }

        // Получаем ежедневную ставку автомобиля из репозитория автомобилей
        Money dailyRate = Money.rubles(request.dailyRate());

        contract.updateDates(newPeriod, dailyRate);
        Contract updated = contractRepository.save(contract);


        return getUserContract(userId, updated.getId().value());


    }


    @Transactional
    @Override
    public ContractDto createContract(Long userId, @NotNull CreateContractRequest request, BigDecimal dailyRate) {

        ClientId clientId = new ClientId(userId);
        log.info("Creating contract for clientId: {}", clientId.value());

        // Проверяем через ACL
        if (!identityPort.isClientVerified(clientId)) {
            throw new EmailNotVerifiedException("Client must verify email and documents");
        }

        log.info("Creating contract for clientId: {}", clientId.value());

        if (!identityPort.isClientActive(clientId)) {
            throw new BannedClientAccessException("Client account is not active");
        }

        log.info("Client {} is verified and active", clientId.value());

        CarId carId = new CarId(request.carId());



        RentalPeriod period = RentalPeriod.of(request.dataStart(), request.dataEnd());

        log.info("Creating contract for carId: {}", carId.value());
        log.info("Period from {} to {}", period.getStartDate(), period.getEndDate());

        if (!rentalDomainService.isCarAvailableForRental(carId, period, null)) {
            throw new CarUnavailableOnDatesException("Car is not available for selected dates");
        }

        Money money = Money.rubles(dailyRate);

        money.multiply(period.getDurationInHours());

        log.info("Daily rate for carId {}: {}", carId.value(), money.getAmount());

        Contract contract = Contract.create(clientId, carId, period, money);
        log.info("Creating contract for carId: {}", carId.value());
        log.info("All information validated, saving contract. ClientId: {}, CarId: {}, Contract: {}", clientId.value(), carId.value(), contract);


        Contract saved = contractRepository.save(contract);
        log.info("Saved contract with id: {}", saved.getId() == null ? "null" : saved.getId().value());
        log.info("Saved contract details: {}", saved.toString());

        log.info("Created contract for carId: {}", carId.value());
        return getUserContract(saved.getId().value(), userId);
    }

    // getters
    @Override
    public ContractDto getUserContract(Long contractId, Long userId) {

        Contract contract = contractRepository.getContractByIdAndClientId(
                        new ContractId(contractId),
                        new ClientId(userId))
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        return contractMapper.toDto(contract);
    }

    @Override
    public Page<ContractDto> getAllContractsByClientId(ClientId clientId, Pageable pageable) {

        Page<Contract> contractsPage = contractRepository.findByClientId(clientId, pageable);

        return contractsPage.map(contractMapper::toDto);
    }

    @Override
    public Page<ContractDto> getAllContractsByAdmin(Pageable pageable, FilterContractRequest filter) {

        RentalStateType status = filter.status() != null ? RentalStateType.valueOf(filter.status()) : null;
        CarId carId = filter.idCar() != null ? new CarId(filter.idCar()) : null;
        ClientId clientId = filter.idUser() != null ? new ClientId(filter.idUser()) : null;

        Page<Contract> contracts = contractRepository.findAllByFilter(
                status,
                clientId,
                carId,
                filter.brand(),
                filter.bodyType(),
                filter.carClass(),
                pageable
        );

        return contracts.map(contractMapper::toDto);
    }

    @Override
    public ContractDto getContractByIdForAdmin(Long contractId) {
        Contract contract = contractRepository.findById(new ContractId(contractId))
                .orElseThrow(() -> new NotFoundException("Contract not found"));
        return contractMapper.toDto(contract);
    }


}
