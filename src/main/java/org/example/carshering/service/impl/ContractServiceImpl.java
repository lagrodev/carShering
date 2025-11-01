package org.example.carshering.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.create.CreateContractRequest;
import org.example.carshering.dto.request.FilterContractRequest;
import org.example.carshering.dto.request.update.UpdateContractRequest;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.entity.Car;
import org.example.carshering.entity.Client;
import org.example.carshering.entity.Contract;
import org.example.carshering.entity.RentalState;
import org.example.carshering.mapper.ContractMapper;
import org.example.carshering.mapper.DocumentMapper;
import org.example.carshering.repository.ContractRepository;
import org.example.carshering.repository.RentalStateRepository;
import org.example.carshering.service.CarService;
import org.example.carshering.service.ClientService;
import org.example.carshering.service.ContractService;
import org.example.carshering.service.DocumentService;
import org.example.carshering.service.domain.RentalDomainService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private final ContractRepository contractRepository;

    private final RentalStateRepository rentalStateRepository;
    private final ContractMapper contractMapper;
    private final DocumentMapper documentMapper;

    private final ClientService clientService;
    private final CarService carService;
    private final DocumentService documentService;
    private final RentalDomainService rentalDomainService;

    private RentalState getStateByName(String name) {
        return rentalStateRepository.findByName(name)
                .orElseThrow(() -> new ValidationException("Состояние " + name + " не найдено"));
    }

    private void ensureState(Contract contract, String expectedState) {
        if (!expectedState.equals(contract.getState().getName())) {
            throw new IllegalStateException("Ожидался статус " + expectedState + ", но текущий: " + contract.getState().getName());
        }
    }

    @Override
    @Transactional
    public ContractResponse createContract(Long userId, CreateContractRequest request) {
        if (!request.dataEnd().isAfter(request.dataStart())) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }

        Client client = clientService.getEntity(userId);


        if (!documentService.hasDocument(userId)) {
            throw new ValidationException("У клиента должен быть загружен документ");
        } // todo проверку документа, в теории сделать, чтобы у человека могло быть несколько документов (права + паспорт, мб. Хотя, прав буд-то достаточно)

        if (!documentService.findDocument(userId).verified()) {
            throw new ValidationException("Документ не подтвержден, дождитесь проверки или прикрепите соответствующий документ");
        }

        Car car = carService.getEntity(request.carId());

        if (!rentalDomainService.isCarAvailable(request.dataStart(), request.dataEnd(), car.getId())) {
            throw new ValidationException("Автомобиль недоступен на выбранные даты");
        }

        Contract contract = contractMapper.toEntity(request);
        contract.setClient(client);
        contract.setCar(car);
        contract.setTotalCost(rentalDomainService.calculateCost(car, request.dataStart(), request.dataEnd()));
        contract.setState(getStateByName("PENDING"));

        return contractMapper.toDto(contractRepository.save(contract));
    }


    @Override
    public void cancelContract(Long userId, Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ValidationException("Контракт не найден"));

        if (!contract.getClient().getId().equals(userId)) {
            throw new ValidationException("Вы не можете отменить чужой контракт");
        }

        cancelContract(contract, false);
    }

    @Override
    public void cancelContractByAdmin(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Контракт не найден"));

        cancelContract(contract, true);
        // админская отмена
    }

    @Override
    @Transactional
    public void confirmCancellationByAdmin(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Контракт не найден"));

        if (!"CANCELLATION_REQUESTED".equals(contract.getState().getName())) {
            throw new IllegalStateException("Контракт не находится на стадии запроса на отмену");
        }

        contract.setState(getStateByName("CANCELLED"));
        contractRepository.save(contract);
    }
    // todo аккуратно
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
            } else if ("CLOSED".equals(currentState)) {
                throw new IllegalStateException("Нельзя отменить завершённый контракт");
            } else {
                throw new IllegalStateException("Невозможно отменить контракт в статусе: " + currentState);
            }
        } else {
            // Пользователь: только PENDING или CONFIRMED -> запрос на отмену
            if ("PENDING".equals(currentState) || "CONFIRMED".equals(currentState)) {
                // todo тут чет сложно надо момент с датой продумать Если сервер в UTC, а пользователь в Москве — возможны ошибки в расчёте daysUntilStart.
                long daysUntilStart = ChronoUnit.DAYS.between(LocalDate.now(), contract.getDataStart());
                if (daysUntilStart > 5) {
                    // Отмена без подтверждения
                    contract.setState(getStateByName("CANCELLED"));
                } else {
                    // Требуется подтверждение
                    contract.setState(getStateByName("CANCELLATION_REQUESTED"));
                }
            }
        }

        contractRepository.save(contract);
    }


    private void setActive(Contract contract) {
        if ("CONFIRMED".equals(contract.getState().getName()) &&
                !contract.getDataStart().isAfter(LocalDate.now())) {
            contract.setState(getStateByName("ACTIVE"));
            contractRepository.save(contract);
        }
    }

    private ContractResponse activateIfDueForDto(Contract contract) {
        ContractResponse dto = contractMapper.toDto(contract);
        if ("CONFIRMED".equals(contract.getState().getName()) &&
                !contract.getDataStart().isAfter(LocalDate.now())) {
            // В DTO временно меняем статус на ACTIVE (но не сохраняем в БД!)
            // Или лучше — добавь поле "effectiveStatus" в ContractResponse
        }
        return dto;
    }

    @Override
    public ContractResponse findContract(Long contractId, Long userId) {
        Contract contract = contractRepository.findByIdAndUserId(contractId, userId)
                .orElseThrow(() -> new ValidationException("Контракт не найден"));
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
    public ContractResponse getContractById(Long contractId) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ValidationException("Контракт не найден"));

        setActive(contract);


        return contractMapper.toDto(contract);
        // todo планировщик, чтобы раз в N минут (например, каждые 5 минут) находить контракты, у которых dataStart <= NOW и статус = CONFIRMED, и активировать их.


    }


    @Override
    @Transactional
    public void confirmContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Контракт не найден"));

        ensureState(contract, "PENDING");

        contract.setState(getStateByName("CONFIRMED"));
        contractRepository.save(contract);
    }

    @Override
    @Transactional
    public ContractResponse updateContract(Long userId, Long contractId, UpdateContractRequest request) {
        // 1. Проверка: дата окончания > начала
        if (!request.dataEnd().isAfter(request.dataStart())) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }

        // 2. Загрузка контракта
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Контракт не найден"));

        // 3. Проверка владельца
        if (!contract.getClient().getId().equals(userId)) {
            throw new ValidationException("Вы не можете изменить чужой контракт");
        }

        // 4. Проверка статуса: только PENDING или CONFIRMED
        String currentState = contract.getState().getName();
        if (!"PENDING".equals(currentState) && !"CONFIRMED".equals(currentState)) {
            throw new IllegalStateException("Изменение доступно только для контрактов в статусе PENDING или CONFIRMED");
        }

        // 5. Проверка доступности авто на новых датах
        if (!rentalDomainService.isCarAvailable(
                request.dataStart(),
                request.dataEnd(),
                contract.getCar().getId()
        )) {
            throw new ValidationException("Автомобиль недоступен на выбранные даты");
        }

        // 6. Обновление
        contractMapper.updateContractFromRequest(request, contract);

        // 7. Пересчёт стоимости
        contract.setTotalCost(rentalDomainService.calculateCost(
                contract.getCar(),
                contract.getDataStart(),
                contract.getDataEnd()
        ));

        return contractMapper.toDto(contractRepository.save(contract));
    }


}
//    @Override
//    public void cancelContractByAdmin(Long contractId) {
//        Contract contract = contractRepository.findById(contractId)
//                .orElseThrow(() -> new EntityNotFoundException("Контракт не найден"));
//
//        RentalState cancelled = rentalStateRepository.findByName("CANCELLED")
//                .orElseThrow(() -> new ValidationException("Состояние CANCELLED не найдено"));
//
//        RentalState closed = rentalStateRepository.findByName("CLOSED")
//                .orElseThrow(() -> new ValidationException("Состояние CLOSED не найдено"));
//
//        if (!contract.getState().equals(closed)) {
//            contract.setState(cancelled);
//            contractRepository.save(contract);
//        } else {
//            throw new IllegalStateException("Отмена доступна только для контрактов в статусе PENDING или CONFIRMED");
//        }
//
//    }
//    @Override
//    public void confirmContract(Long contractId) {
//        Contract contract = contractRepository.findById(contractId)
//                .orElseThrow(() -> new RuntimeException("Contract not found"));
//
//
//        if (!"PENDING".equals(contract.getState().getName())) {
//            throw new RuntimeException("Cannot confirm this contract");
//        }
//
//        RentalState confirmed = rentalStateRepository.findByName("ACTIVE")
//                .orElseThrow();
//        contract.setState(confirmed);
//        contractRepository.save(contract);
//    }