package org.example.carshering.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.dto.request.FilterContractRequest;
import org.example.carshering.dto.request.create.CreateContractRequest;
import org.example.carshering.dto.request.update.UpdateContractRequest;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.entity.Car;
import org.example.carshering.entity.Client;
import org.example.carshering.entity.Contract;
import org.example.carshering.entity.RentalState;
import org.example.carshering.exceptions.custom.*;
import org.example.carshering.mapper.ContractMapper;
import org.example.carshering.repository.ContractRepository;
import org.example.carshering.repository.RentalStateRepository;
import org.example.carshering.service.domain.CarServiceHelperService;
import org.example.carshering.service.domain.ClientServiceHelper;
import org.example.carshering.service.domain.DocumentServiceHelper;
import org.example.carshering.service.domain.RentalDomainService;
import org.example.carshering.service.interfaces.ContractService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
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
    private final ContractMapper contractMapper;
    private final ClientServiceHelper clientService;
    private final CarServiceHelperService carService;
    private final DocumentServiceHelper documentService;
    private final RentalDomainService rentalDomainService;

    private RentalState getStateByName(String name) {
        return rentalStateRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NotFoundException("State " + name + " not found"));
    }

    private void ensureState(Contract contract, String expectedState) {
        if (!expectedState.equals(contract.getState().getName())) {
            throw new InvalidContractStateException("Status expected " + expectedState + "but current: "
                    + contract.getState().getName());
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

        if (!rentalDomainService.isCarAvailable(request.dataStart(), request.dataEnd(), car.getId(), null)) {
            throw new CarUnavailableOnDatesException("The car is not available on the selected dates");
        }
        Contract contract = contractMapper.toEntity(request);
        contract.setClient(client);
        contract.setCar(car);
        contract.setTotalCost(rentalDomainService.calculateCost(car, request.dataStart(), request.dataEnd()));
        contract.setState(getStateByName("PENDING"));
        return contractMapper.toDto(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public void cancelContract(Long userId, Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        log.info("Contract client id = {}, Current user id = {}",
                contract.getClient().getId(), userId);

        if (!contract.getClient().getId().equals(userId)) {
            throw new UnauthorizedContractAccessException("You can't terminate someone else's contract");
        }

        cancelContract(contract, false);
    }

    // FIXME: сделать что-то типо подписи для админской отмены контракта, чтобы не любой админ мог отменять контракты
    @Override
    @Transactional
    public void cancelContractByAdmin(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        cancelContract(contract, true);
        // админская отмена
    }

    @Override
    @Transactional
    public void confirmCancellationByAdmin(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        if (!"CANCELLATION_REQUESTED".equals(contract.getState().getName())) {
            throw new InvalidContractCancellationStateException("The contract is not at the cancellation request stage");
        }

        contract.setState(getStateByName("CANCELLED"));
        contractRepository.save(contract);
    }


    @Override
    @Transactional
    public ContractResponse findContract(Long contractId, Long userId) {
        Contract contract = contractRepository.findByIdAndUserId(contractId, userId)
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

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        setActive(contract);


        return contractMapper.toDto(contract);
        // todo планировщик, чтобы раз в N минут (например, каждые 5 минут) находить контракты, у которых dataStart <= NOW и статус = CONFIRMED, и активировать их.


    }

    @Override
    @Transactional
    public ContractResponse confirmContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        ensureState(contract, "PENDING");

        contract.setState(getStateByName("CONFIRMED"));
        Duration duration = Duration.between(contract.getDataStart(), contract.getDataEnd());
        long minutes = Math.max(0, duration.toMinutes());
        contract.setDurationMinutes(minutes);
        contractRepository.save(contract);
        return contractMapper.toDto(contract);
    }


    @Override
    public void checkAndAllActiveContractsByClient(Client client) {
        List<Contract> activeContracts = contractRepository.findAllByClientAndActiveStates(client, ACTIVE_STATES);
        if (!activeContracts.isEmpty()) {
            Contract c = activeContracts.getFirst();
            throw new BusinessConflictException("Active contract exists: ID " + c.getId() + ", state: " + c.getState().getName());
        }

    }


    private ContractResponse activateIfDueForDto(Contract contract) {

        RentalState now = contract.getState();


        if ("CONFIRMED".equals(contract.getState().getName()) &&
                !contract.getDataStart().isAfter(LocalDateTime.now())) {
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
    protected void cancelContract(Contract contract, boolean isAdmin) {
        String currentState = contract.getState().getName();

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
                long daysUntilStart = ChronoUnit.DAYS.between(LocalDateTime.now(), contract.getDataStart());
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
    void setActive(Contract contract) {
        if ("CONFIRMED".equals(contract.getState().getName()) &&
                !contract.getDataStart().isAfter(LocalDateTime.now())) {
            contract.setState(getStateByName("ACTIVE"));
            contractRepository.save(contract);
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *") // каждый час
    public void setActive() {
        LocalDateTime now = LocalDateTime.now();


        List<Contract> contractsToActivate = contractRepository
                .findAllByStateNameAndDataStartBefore("CONFIRMED", now);

        if (contractsToActivate.isEmpty()) return;

        RentalState activeState = getStateByName("ACTIVE");
        for (Contract contract : contractsToActivate) {
            contract.setState(activeState);
        }
        contractRepository.saveAll(contractsToActivate);
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *") // каждый час
    public void setCompleted() {
        LocalDateTime now = LocalDateTime.now();


        List<Contract> contractsToComplete = contractRepository
                .findAllByStateNameAndDataEndBefore("ACTIVE", now);

        if (contractsToComplete.isEmpty()) return;

        RentalState completedState = getStateByName("COMPLETED");
        for (Contract contract : contractsToComplete) {
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

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        if (!contract.getClient().getId().equals(userId)) {
            throw new UnauthorizedContractAccessException("You can't terminate someone else's contract");
        }

        String currentState = contract.getState().getName();
        if (!"PENDING".equals(currentState) && !"CONFIRMED".equals(currentState)) {
            throw new CannotCancelCompletedContractException("The change is only available for contracts in the status PENDING or CONFIRMED");
        }

        if (!rentalDomainService.isCarAvailable(
                request.dataStart(),
                request.dataEnd(),
                contract.getCar().getId(),
                contract.getId())) {
            throw new CarUnavailableOnDatesException("The car is not available on the selected dates");
        }

        contractMapper.updateContractFromRequest(request, contract);


        contract.setTotalCost(rentalDomainService.calculateCost(
                contract.getCar(),
                contract.getDataStart(),
                contract.getDataEnd()
        ));

        return contractMapper.toDto(contractRepository.save(contract));
    }


}
