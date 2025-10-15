package org.example.carshering.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.CreateContractRequest;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.entity.Car;
import org.example.carshering.entity.Client;
import org.example.carshering.entity.Contract;
import org.example.carshering.entity.RentalState;
import org.example.carshering.mapper.ContractMapper;
import org.example.carshering.repository.*;
import org.example.carshering.service.CarService;
import org.example.carshering.service.ContractService;
import org.example.carshering.service.DocumentService;
import org.example.carshering.service.ClientService;
import org.example.carshering.service.domain.RentalDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private final ContractRepository contractRepository;

    private final RentalStateRepository rentalStateRepository;
    private final ContractMapper contractMapper;

    private final ClientService clientService;
    private final CarService carService;
    private final DocumentService documentService;
    private final RentalDomainService rentalDomainService;


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


        Car car = carService.getEntity(request.carId());

        if (!rentalDomainService.isCarAvailable(request.dataStart(), request.dataEnd(), car.getId())) {
            throw new ValidationException("Автомобиль недоступен на выбранные даты");
        }

        Contract contract = contractMapper.toEntity(request);
        contract.setClient(client);
        contract.setCar(car);
        contract.setTotalCost(rentalDomainService.calculateCost(car, request.dataStart(), request.dataEnd()));
        contract.setState(getBookedState());

        return contractMapper.toDto(contractRepository.save(contract));
    }

    private RentalState getBookedState() {
        return rentalStateRepository.findByName("PENDING")// в ожидании // todo надо будет потом для удобства сделать фильтрацию по этим статусам, для админа хотя бы
                .orElseThrow(() -> new ValidationException("PENDING статус не найден"));
    }

    @Override
    public void cancelContract(Long userId, Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ValidationException("Контракт не найден"));

        if (!contract.getClient().getId().equals(userId)) {
            throw new ValidationException("Вы не можете отменить чужой контракт");
        }

        cancelContract(contractId);
    }

    @Override
    public void cancelContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Контракт не найден"));

        RentalState cancelled = rentalStateRepository.findByName("CANCELLED")
                .orElseThrow(() -> new ValidationException("Состояние CANCELLED не найдено"));

        contract.setState(cancelled);
        contractRepository.save(contract);
    } // todo логику реальной отмены: пользователь отменил, но одмин должен или поджтвердить, или день старта должен быть не позже чем через 5 дней


    @Override
    public ContractResponse findContract(Long contractId, Long userId) {
        Contract contract = contractRepository.findByIdAndUserId(contractId, userId)
                .orElseThrow(() -> new ValidationException("Контракт не найден"));
        return contractMapper.toDto(contract);
    }

    @Override
    public List<ContractResponse> getAllContracts(Long userId) {
        return contractRepository.findByClientId(userId)
                .stream()
                .map(contractMapper::toDto)
                .toList();
    }

    @Override
    public List<ContractResponse> getAllContracts() {
        return contractRepository.findAll()
                .stream()
                .map(contractMapper::toDto)
                .toList();
    }

    @Override
    public ContractResponse getContractById(Long contractId) {
        return contractMapper.toDto(contractRepository
                .findById(contractId)
                .orElseThrow(() -> new ValidationException("Контракт не найден")));

    }

    @Override
    public void confirmContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));


        if (!"PENDING".equals(contract.getState().getName())) {
            throw new RuntimeException("Cannot confirm this contract");
        }

        RentalState confirmed = rentalStateRepository.findByName("CONFIRMED")
                .orElseThrow();
        contract.setState(confirmed);
        contractRepository.save(contract);
    }


}
