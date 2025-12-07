package org.example.carshering.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.dto.request.FilterContractRequest;
import org.example.carshering.dto.request.create.CreateContractRequest;
import org.example.carshering.dto.request.update.UpdateContractRequest;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.domain.entity.Car;
import org.example.carshering.domain.entity.Client;
import org.example.carshering.mapper.ContractMapperOld;
import org.example.carshering.rental.domain.valueobject.RentalStateType;
import org.example.carshering.rental.infrastructure.persistence.entity.ContractJpaEntity;
import org.example.carshering.exceptions.custom.*;
import org.example.carshering.rental.infrastructure.persistence.repository.ContractRepository;
import org.example.carshering.repository.RentalStateRepository;
import org.example.carshering.service.domain.CarServiceHelperService;
import org.example.carshering.service.domain.ClientServiceHelper;
import org.example.carshering.service.domain.DocumentServiceHelper;
import org.example.carshering.service.domain.RentalDomainServiceOld;
import org.example.carshering.service.interfaces.ContractService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractServiceImpl implements ContractService {
    private static final Set<String> ACTIVE_STATES = Set.of("ACTIVE", "PENDING", "CANCELLATION REQUESTED", "CONFIRMED");
    private final ContractRepository contractRepository;
    private final RentalStateRepository rentalStateRepository;
    private final ContractMapperOld contractMapper;
    private final ClientServiceHelper clientService;
    private final CarServiceHelperService carService;
    private final DocumentServiceHelper documentService;
    private final RentalDomainServiceOld rentalDomainServiceOld;

    private RentalStateType getStateByName(String name) {
        return null;

//                rentalStateRepository.findByNameIgnoreCase(name)
//                .orElseThrow(() -> new NotFoundException("State " + name + " not found"));
    }

    private void ensureState(ContractJpaEntity contract, String expectedState) {
        if (!expectedState.equals(contract.getState().name())) {
            throw new InvalidContractStateException("Status expected " + expectedState + "but current: "
                    + contract.getState().name());
        }
    }


    @Override
    @Transactional
    public ContractResponse createContract(Long userId, CreateContractRequest request) {
        if (!request.dataEnd().isAfter(request.dataStart())) {
            throw new InvalidContractDateRangeException("The end date must be later than the start date");
        }

        Client client = clientService.getEntity(userId);

        if (!documentService.hasDocument(userId)) {
            throw new MissingClientDocumentException("The client must have a document uploaded");
        }

        if (!documentService.findDocument(userId).verified()) {
            throw new UnverifiedClientDocumentException("The document is not verified. Please wait for verification or attach the relevant document");
        }

        Car car = carService.getEntityWithLock(request.carId());

        if (!rentalDomainServiceOld.isCarAvailable(request.dataStart(), request.dataEnd(), car.getId(), null)) {
            throw new CarUnavailableOnDatesException("The car is not available on the selected dates");
        }

        ContractJpaEntity contract = contractMapper.toEntity(request);
        contract.setClientId(client.getId());
        contract.setCarId(car.getId());
        contract.setTotalCost(rentalDomainServiceOld.calculateCost(car, request.dataStart(), request.dataEnd()));
        contract.setState(getStateByName("PENDING"));
        return contractMapper.toDto(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public void cancelContract(Long userId, Long contractId) {
        ContractJpaEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        log.info("Contract client id = {}, Current user id = {}",
                contract.getClientId(), userId);

        if (!contract.getClientId().equals(userId)) {
            throw new UnauthorizedContractAccessException("You can't terminate someone else's contract");
        }

        cancelContract(contract, false);
    }

    // FIXME: сделать что-то типо подписи для админской отмены контракта, чтобы не любой админ мог отменять контракты
    @Override
    @Transactional
    public void cancelContractByAdmin(Long contractId) {
        ContractJpaEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        cancelContract(contract, true);
        // админская отмена
    }

    @Override
    @Transactional
    public void confirmCancellationByAdmin(Long contractId) {
        ContractJpaEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        if (!"CANCELLATION_REQUESTED".equals(contract.getState().name())) {
            throw new InvalidContractCancellationStateException("The contract is not at the cancellation request stage");
        }

        contract.setState(getStateByName("CANCELLED"));
        contractRepository.save(contract);
    }


    @Override
    @Transactional
    public ContractResponse findContract(Long contractId, Long userId) {
        ContractJpaEntity contract = contractRepository.findByIdAndUserId(contractId, userId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));
        setActive(contract);

        return contractMapper.toDto(contract);
    }

    @Override
    public Page<ContractResponse> getAllClientContracts(Pageable pageable, Long userId) {
        return contractRepository.findByClientId(userId, pageable)
                .map(this::activateIfDueForDto);
    }

    @Override
    public Page<ContractResponse> getAllContracts(Pageable pageable, FilterContractRequest filter) {
        return contractRepository.findAllByFilter(
                        filter.status(),
                        filter.idUser(),
                        filter.idCar(),
                        filter.brand(),
                        filter.bodyType(),
                        filter.carClass(),
                        pageable
                )
                .map(this::activateIfDueForDto);
    }

    @Override
    @Transactional
    public ContractResponse getContractById(Long contractId) {

        ContractJpaEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        setActive(contract);


        return contractMapper.toDto(contract);
        // todo планировщик, чтобы раз в N минут (например, каждые 5 минут) находить контракты, у которых dataStart <= NOW и статус = CONFIRMED, и активировать их.


    }

    @Override
    @Transactional
    public ContractResponse confirmContract(Long contractId) {
        ContractJpaEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        ensureState(contract, "PENDING");

        contract.setState(getStateByName("CONFIRMED"));

        contractRepository.save(contract);

        return contractMapper.toDto(contract);
    }


    @Override
    public void checkAndAllActiveContractsByClient(Client client) {
        List<ContractJpaEntity> activeContracts = contractRepository.findAllByClientAndActiveStates(client, ACTIVE_STATES);
        if (!activeContracts.isEmpty()) {
            ContractJpaEntity c = activeContracts.getFirst();
            throw new BusinessConflictException("Active contract exists: ID " + c.getId() + ", state: " + c.getState().name());
        }

    }


    private ContractResponse activateIfDueForDto(ContractJpaEntity contract) {

        RentalStateType now = contract.getState();


        if ("CONFIRMED".equals(contract.getState().name()) &&
                !contract.getPeriod().getStartDate().isAfter(LocalDateTime.now())) {
            // В DTO временно меняем статус на ACTIVE (но не сохраняем в БД!)


            contract.setState(getStateByName("ACTIVE"));

        }
        ContractResponse dto = contractMapper.toDto(contract);
        contract.setState(now); // возвращаем обратно
        return dto;
    }

    // FIXME: продумать логику отмены контракта
    // FIXME: аккуратно
    @Transactional
    protected void cancelContract(ContractJpaEntity contract, boolean isAdmin) {
        String currentState = contract.getState().name();

        if ("CANCELLED".equals(currentState)) {
            return;
        }

        if (isAdmin) {
            // Админ может отменить любой запрос на отмену или подтвердить отмену
            if ("CANCELLATION_REQUESTED".equals(currentState) ||
                    "PENDING".equals(currentState) ||
                    "CONFIRMED".equals(currentState)) {
                contract.setState(getStateByName("CANCELLED"));
            } else {
                throw new CannotCancelCompletedContractException("It is not possible to cancel a contract that has the following state:" + currentState);
            }
        } else {
            // Пользователь: только PENDING или CONFIRMED -> запрос на отмену
            if ("PENDING".equals(currentState) || "CONFIRMED".equals(currentState)) {
                // todo тут чет сложно надо момент с датой продумать Если сервер в UTC, а пользователь в Москве — возможны ошибки в расчёте daysUntilStart.
                long daysUntilStart = ChronoUnit.DAYS.between(LocalDateTime.now(), contract.getPeriod().getStartDate());
                if (daysUntilStart > 5) {
                    // Отмена без подтверждения
                    contract.setState(getStateByName("CANCELLED"));
                } else {
                    // Требуется подтверждение
                    contract.setState(getStateByName("CANCELLATION_REQUESTED"));
                }
            } else
                throw new CannotCancelCompletedContractException("It is not possible to cancel a contract that has the following state:" + currentState);

        }

        contractRepository.save(contract);
    }

    @Transactional
    void setActive(ContractJpaEntity contract) {
        if ("CONFIRMED".equals(contract.getState().name()) &&
                !contract.getPeriod().getStartDate().isAfter(LocalDateTime.now())) {
            contract.setState(getStateByName("ACTIVE"));
            contractRepository.save(contract);
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *") // каждый час
    public void setActive() {
        LocalDateTime now = LocalDateTime.now();


        List<ContractJpaEntity> contractsToActivate = contractRepository
                .findAllByStateNameAndStartDateBefore("CONFIRMED", now);

        if (contractsToActivate.isEmpty()) return;

        RentalStateType activeState = getStateByName("ACTIVE");
        for (ContractJpaEntity contract : contractsToActivate) {
            contract.setState(activeState);
        }
        contractRepository.saveAll(contractsToActivate);
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *") // каждый час
    public void setCompleted() {
        LocalDateTime now = LocalDateTime.now();


        List<ContractJpaEntity> contractsToComplete = contractRepository
                .findAllByStateNameAndDataEndBefore("ACTIVE", now);

        if (contractsToComplete.isEmpty()) return;

        RentalStateType completedState = getStateByName("COMPLETED");
        for (ContractJpaEntity contract : contractsToComplete) {
            contract.setState(completedState);
        }
        contractRepository.saveAll(contractsToComplete);
    }


    @Override
    @Transactional
    public ContractResponse updateContract(Long userId, Long contractId, UpdateContractRequest request) {
        if (!request.dataEnd().isAfter(request.dataStart())) {
            throw new InvalidContractDateRangeException("The end date must be later than the start date");
        }

        ContractJpaEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        if (!contract.getClientId().equals(userId)) {
            throw new UnauthorizedContractAccessException("You can't terminate someone else's contract");
        }

        String currentState = contract.getState().name();
        if (!"PENDING".equals(currentState) && !"CONFIRMED".equals(currentState)) {
            throw new CannotCancelCompletedContractException("The change is only available for contracts in the status PENDING or CONFIRMED");
        }

        if (!rentalDomainServiceOld.isCarAvailable(
                request.dataStart(),
                request.dataEnd(),
                contract.getCarId(),
                contract.getId())) {
            throw new CarUnavailableOnDatesException("The car is not available on the selected dates");
        }

        contractMapper.updateContractFromRequest(request, contract);


        Car car = carService.getEntity(contract.getCarId());

        contract.setTotalCost(rentalDomainServiceOld.calculateCost(
                car,
                contract.getPeriod().getStartDate(),
                contract.getPeriod().getEndDate()
        ));

        return contractMapper.toDto(contractRepository.save(contract));
    }


}
