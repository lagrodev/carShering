package org.example.carshering.service.impl;

import jakarta.validation.ValidationException;
import org.example.carshering.dto.request.FilterContractRequest;
import org.example.carshering.dto.request.create.CreateContractRequest;
import org.example.carshering.dto.request.update.UpdateContractRequest;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.dto.response.DocumentResponse;
import org.example.carshering.entity.Car;
import org.example.carshering.entity.Client;
import org.example.carshering.entity.Contract;
import org.example.carshering.entity.RentalState;
import org.example.carshering.exceptions.custom.*;
import org.example.carshering.mapper.ContractMapper;
import org.example.carshering.repository.ContractRepository;
import org.example.carshering.repository.RentalStateRepository;
import org.example.carshering.service.CarService;
import org.example.carshering.service.ClientService;
import org.example.carshering.service.DocumentService;
import org.example.carshering.service.domain.RentalDomainService;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContractServiceImplTests {

    private final DataUtils dataUtils = new DataUtils();
    @Mock
    private ContractRepository contractRepository;
    @Mock
    private RentalStateRepository rentalStateRepository;
    @Mock
    private ContractMapper contractMapper;
    @Mock
    private ClientService clientService;
    @Mock
    private CarService carService;
    @Mock
    private DocumentService documentService;
    @Mock
    private RentalDomainService rentalDomainService;
    @InjectMocks
    private ContractServiceImpl serviceUnderTest;

    @Test
    @DisplayName("Test create contract functionality")
    public void givenValidContractRequest_whenCreateContract_thenContractIsCreated() {
        // given
        Long userId = 1L;
        CreateContractRequest request = new CreateContractRequest(
                1L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );

        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(userId);

        Car car = dataUtils.getJohnDoeTransient(
                dataUtils.getCarStateTransient("AVAILABLE"),
                dataUtils.getCarModelSEDAN(
                        dataUtils.getBrandTransient(),
                        dataUtils.getModelNameTransient(),
                        dataUtils.getCarClassTransient()
                )
        );
        car.setId(1L);

        RentalState pendingState = dataUtils.getRentalState("PENDING");
        pendingState.setId(1L);

        Contract contractEntity = dataUtils.createContract(
                client,
                car,
                pendingState,
                request.dataStart(),
                request.dataEnd()
        );

        Contract savedContract = dataUtils.createContract(
                client,
                car,
                pendingState,
                request.dataStart(),
                request.dataEnd()
        );
        savedContract.setId(1L);

        ContractResponse response = new ContractResponse(
                1L, 100.0, "Brand", "Model", "SEDAN", "Class", 2020,
                "Last", request.dataStart(), request.dataEnd(), "VIN123", "GOS123", "PENDING"
        );

        DocumentResponse documentResponse = DataUtils.documentResponsePersisted("PASSPORT", "1234", "567890", "Authority", true);

        given(clientService.getEntity(userId)).willReturn(client);
        given(documentService.hasDocument(userId)).willReturn(true);
        given(documentService.findDocument(userId)).willReturn(documentResponse);
        given(carService.getEntity(request.carId())).willReturn(car);
        given(rentalDomainService.isCarAvailable(request.dataStart(), request.dataEnd(), car.getId())).willReturn(true);
        given(contractMapper.toEntity(request)).willReturn(contractEntity);
        given(rentalDomainService.calculateCost(car, request.dataStart(), request.dataEnd())).willReturn(100.0);
        given(rentalStateRepository.findByNameIgnoreCase("PENDING")).willReturn(Optional.of(pendingState));
        given(contractRepository.save(contractEntity)).willReturn(savedContract);
        given(contractMapper.toDto(savedContract)).willReturn(response);

        // when
        ContractResponse actual = serviceUnderTest.createContract(userId, request);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(1L);
        assertThat(actual.state()).isEqualTo("PENDING");
        assertThat(actual.totalCost()).isEqualTo(100.0);

        verify(clientService).getEntity(userId);
        verify(documentService).hasDocument(userId);
        verify(documentService).findDocument(userId);
        verify(carService).getEntity(request.carId());
        verify(rentalDomainService).isCarAvailable(request.dataStart(), request.dataEnd(), car.getId());
        verify(contractRepository).save(contractEntity);
        verify(contractMapper).toDto(savedContract);
    }

    @Test
    @DisplayName("Test create contract with end date before start date throws exception")
    public void givenInvalidDateRange_whenCreateContract_thenThrowException() {
        // given
        Long userId = 1L;
        CreateContractRequest request = new CreateContractRequest(
                1L,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(1)
        );

        // when + then
        assertThrows(
                InvalidContractDateRangeException.class,
                () -> serviceUnderTest.createContract(userId, request)
        );

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test create contract with start date in past throws exception")
    public void givenStartDateInPast_whenCreateContract_thenThrowException() {
        // given
        Long userId = 1L;
        CreateContractRequest request = new CreateContractRequest(
                1L,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5)
        );

        // when + then
        assertThrows(
                ValidationException.class,
                () -> serviceUnderTest.createContract(userId, request)
        );

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test create contract without client document throws exception")
    public void givenClientWithoutDocument_whenCreateContract_thenThrowException() {
        // given
        Long userId = 1L;
        CreateContractRequest request = new CreateContractRequest(
                1L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );

        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(userId);

        given(clientService.getEntity(userId)).willReturn(client);
        given(documentService.hasDocument(userId)).willReturn(false);

        // when + then
        assertThrows(
                MissingClientDocumentException.class,
                () -> serviceUnderTest.createContract(userId, request)
        );

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test create contract with unverified document throws exception")
    public void givenUnverifiedDocument_whenCreateContract_thenThrowException() {
        // given
        Long userId = 1L;
        CreateContractRequest request = new CreateContractRequest(
                1L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );

        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(userId);

        DocumentResponse unverifiedDocument = DataUtils.documentResponsePersisted("PASSPORT", "1234", "567890", "Authority", false);

        given(clientService.getEntity(userId)).willReturn(client);
        given(documentService.hasDocument(userId)).willReturn(true);
        given(documentService.findDocument(userId)).willReturn(unverifiedDocument);

        // when + then
        assertThrows(
                UnverifiedClientDocumentException.class,
                () -> serviceUnderTest.createContract(userId, request)
        );

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test create contract with unavailable car throws exception")
    public void givenUnavailableCar_whenCreateContract_thenThrowException() {
        // given
        Long userId = 1L;
        CreateContractRequest request = new CreateContractRequest(
                1L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );

        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(userId);

        Car car = dataUtils.getJohnDoeTransient(
                dataUtils.getCarStateTransient("AVAILABLE"),
                dataUtils.getCarModelSEDAN(
                        dataUtils.getBrandTransient(),
                        dataUtils.getModelNameTransient(),
                        dataUtils.getCarClassTransient()
                )
        );
        car.setId(1L);

        DocumentResponse documentResponse = DataUtils.documentResponsePersisted("PASSPORT", "1234", "567890", "Authority", true);

        given(clientService.getEntity(userId)).willReturn(client);
        given(documentService.hasDocument(userId)).willReturn(true);
        given(documentService.findDocument(userId)).willReturn(documentResponse);
        given(carService.getEntity(request.carId())).willReturn(car);
        given(rentalDomainService.isCarAvailable(request.dataStart(), request.dataEnd(), car.getId())).willReturn(false);

        // when + then
        assertThrows(
                CarUnavailableOnDatesException.class,
                () -> serviceUnderTest.createContract(userId, request)
        );

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test confirm contract functionality")
    public void givenPendingContract_whenConfirmContract_thenContractIsConfirmed() {
        // given
        Long contractId = 1L;

        RentalState pendingState = dataUtils.getRentalState("PENDING");
        RentalState confirmedState = dataUtils.getRentalState("CONFIRMED");
        confirmedState.setId(2L);

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                pendingState,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(contractId);

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));
        given(rentalStateRepository.findByNameIgnoreCase("CONFIRMED")).willReturn(Optional.of(confirmedState));
        given(contractRepository.save(contract)).willReturn(contract);

        // when
        serviceUnderTest.confirmContract(contractId);

        // then
        verify(contractRepository).findById(contractId);
        verify(rentalStateRepository).findByNameIgnoreCase("CONFIRMED");
        verify(contractRepository).save(contract);
    }

    @Test
    @DisplayName("Test confirm contract with non-pending state throws exception")
    public void givenNonPendingContract_whenConfirmContract_thenThrowException() {
        // given
        Long contractId = 1L;

        RentalState activeState = dataUtils.getRentalState("ACTIVE");

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                activeState,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(contractId);

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));

        // when + then
        assertThrows(
                InvalidContractStateException.class,
                () -> serviceUnderTest.confirmContract(contractId)
        );

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test confirm contract with non-existing contract throws exception")
    public void givenNonExistingContract_whenConfirmContract_thenThrowException() {
        // given
        Long contractId = 1L;

        given(contractRepository.findById(contractId)).willReturn(Optional.empty());

        // when + then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.confirmContract(contractId)
        );

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test cancel contract by user functionality")
    public void givenUserContract_whenCancelContract_thenContractIsCancelled() {
        // given
        Long userId = 1L;
        Long contractId = 1L;

        Client client = dataUtils.createAndSaveClient("test", "test@test.com");
        client.setId(userId);

        RentalState pendingState = dataUtils.getRentalState("PENDING");
        RentalState cancelledState = dataUtils.getRentalState("CANCELLED");
        cancelledState.setId(2L);

        Contract contract = dataUtils.createContract(
                client,
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                pendingState,
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(15)
        );
        contract.setId(contractId);

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));
        given(rentalStateRepository.findByNameIgnoreCase("CANCELLED")).willReturn(Optional.of(cancelledState));
        given(contractRepository.save(contract)).willReturn(contract);

        // when
        serviceUnderTest.cancelContract(userId, contractId);

        // then
        verify(contractRepository).findById(contractId);
        verify(rentalStateRepository).findByNameIgnoreCase("CANCELLED");
        verify(contractRepository).save(contract);
    }

    @Test
    @DisplayName("Test cancel contract by unauthorized user throws exception")
    public void givenUnauthorizedUser_whenCancelContract_thenThrowException() {
        // given
        Long userId = 1L;
        Long contractId = 1L;

        Client client = dataUtils.createAndSaveClient("test", "test@test.com");
        client.setId(2L);

        RentalState pendingState = dataUtils.getRentalState("PENDING");

        Contract contract = dataUtils.createContract(
                client,
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                pendingState,
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(15)
        );
        contract.setId(contractId);

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));

        // when + then
        assertThrows(
                UnauthorizedContractAccessException.class,
                () -> serviceUnderTest.cancelContract(userId, contractId)
        );

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test cancel contract with less than 5 days creates cancellation request")
    public void givenContractStartsInLessThan5Days_whenCancelContract_thenCancellationRequestIsCreated() {
        // given
        Long userId = 1L;
        Long contractId = 1L;

        Client client = dataUtils.createAndSaveClient("test", "test@test.com");
        client.setId(userId);

        RentalState confirmedState = dataUtils.getRentalState("CONFIRMED");
        RentalState cancellationRequestedState = dataUtils.getRentalState("CANCELLATION_REQUESTED");
        cancellationRequestedState.setId(2L);

        Contract contract = dataUtils.createContract(
                client,
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                confirmedState,
                LocalDate.now().plusDays(3),
                LocalDate.now().plusDays(7)
        );
        contract.setId(contractId);

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));
        given(rentalStateRepository.findByNameIgnoreCase("CANCELLATION_REQUESTED")).willReturn(Optional.of(cancellationRequestedState));
        given(contractRepository.save(contract)).willReturn(contract);

        // when
        serviceUnderTest.cancelContract(userId, contractId);

        // then
        verify(contractRepository).findById(contractId);
        verify(rentalStateRepository).findByNameIgnoreCase("CANCELLATION_REQUESTED");
        verify(contractRepository).save(contract);
    }

    @Test
    @DisplayName("Test cancel contract by admin functionality")
    public void givenAdminCancellation_whenCancelContractByAdmin_thenContractIsCancelled() {
        // given
        Long contractId = 1L;

        RentalState pendingState = dataUtils.getRentalState("PENDING");
        RentalState cancelledState = dataUtils.getRentalState("CANCELLED");
        cancelledState.setId(2L);

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                pendingState,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(contractId);

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));
        given(rentalStateRepository.findByNameIgnoreCase("CANCELLED")).willReturn(Optional.of(cancelledState));
        given(contractRepository.save(contract)).willReturn(contract);

        // when
        serviceUnderTest.cancelContractByAdmin(contractId);

        // then
        verify(contractRepository).findById(contractId);
        verify(rentalStateRepository).findByNameIgnoreCase("CANCELLED");
        verify(contractRepository).save(contract);
    }

    @Test
    @DisplayName("Test confirm cancellation by admin functionality")
    public void givenCancellationRequestedContract_whenConfirmCancellationByAdmin_thenContractIsCancelled() {
        // given
        Long contractId = 1L;

        RentalState cancellationRequestedState = dataUtils.getRentalState("CANCELLATION_REQUESTED");
        RentalState cancelledState = dataUtils.getRentalState("CANCELLED");
        cancelledState.setId(2L);

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                cancellationRequestedState,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(contractId);

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));
        given(rentalStateRepository.findByNameIgnoreCase("CANCELLED")).willReturn(Optional.of(cancelledState));
        given(contractRepository.save(contract)).willReturn(contract);

        // when
        serviceUnderTest.confirmCancellationByAdmin(contractId);

        // then
        verify(contractRepository).findById(contractId);
        verify(rentalStateRepository).findByNameIgnoreCase("CANCELLED");
        verify(contractRepository).save(contract);
    }

    @Test
    @DisplayName("Test confirm cancellation with wrong state throws exception")
    public void givenNonCancellationRequestedState_whenConfirmCancellationByAdmin_thenThrowException() {
        // given
        Long contractId = 1L;

        RentalState activeState = dataUtils.getRentalState("ACTIVE");

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                activeState,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(contractId);

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));

        // when + then
        assertThrows(
                InvalidContractCancellationStateException.class,
                () -> serviceUnderTest.confirmCancellationByAdmin(contractId)
        );

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test find contract by id and user functionality")
    public void givenContractIdAndUserId_whenFindContract_thenContractIsReturned() {
        // given
        Long contractId = 1L;
        Long userId = 1L;

        Client client = dataUtils.createAndSaveClient("test", "test@test.com");
        client.setId(userId);

        RentalState pendingState = dataUtils.getRentalState("PENDING");

        Contract contract = dataUtils.createContract(
                client,
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                pendingState,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(contractId);

        ContractResponse response = new ContractResponse(
                1L, 100.0, "Brand", "Model", "SEDAN", "Class", 2020,
                "Last", contract.getDataStart(), contract.getDataEnd(), "VIN123", "GOS123", "PENDING"
        );

        given(contractRepository.findByIdAndUserId(contractId, userId)).willReturn(Optional.of(contract));
        given(contractMapper.toDto(contract)).willReturn(response);

        // when
        ContractResponse actual = serviceUnderTest.findContract(contractId, userId);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(1L);
        assertThat(actual.state()).isEqualTo("PENDING");

        verify(contractRepository).findByIdAndUserId(contractId, userId);
        verify(contractMapper).toDto(contract);
    }

    @Test
    @DisplayName("Test find contract with non-existing contract throws exception")
    public void givenNonExistingContract_whenFindContract_thenThrowException() {
        // given
        Long contractId = 1L;
        Long userId = 1L;

        given(contractRepository.findByIdAndUserId(contractId, userId)).willReturn(Optional.empty());

        // when + then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.findContract(contractId, userId)
        );

        verify(contractMapper, never()).toDto(any(Contract.class));
    }

    @Test
    @DisplayName("Test get contract by id functionality")
    public void givenContractId_whenGetContractById_thenContractIsReturned() {
        // given
        Long contractId = 1L;

        RentalState pendingState = dataUtils.getRentalState("PENDING");

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                pendingState,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(contractId);

        ContractResponse response = new ContractResponse(
                1L, 100.0, "Brand", "Model", "SEDAN", "Class", 2020,
                "Last", contract.getDataStart(), contract.getDataEnd(), "VIN123", "GOS123", "PENDING"
        );

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));
        given(contractMapper.toDto(contract)).willReturn(response);

        // when
        ContractResponse actual = serviceUnderTest.getContractById(contractId);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(1L);
        assertThat(actual.state()).isEqualTo("PENDING");

        verify(contractRepository).findById(contractId);
        verify(contractMapper).toDto(contract);
    }

    @Test
    @DisplayName("Test get all client contracts functionality")
    public void givenUserId_whenGetAllClientContracts_thenPageIsReturned() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Client client = dataUtils.createAndSaveClient("test", "test@test.com");
        client.setId(userId);

        RentalState pendingState = dataUtils.getRentalState("PENDING");

        Contract contract1 = dataUtils.createContract(
                client,
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                pendingState,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract1.setId(1L);

        Contract contract2 = dataUtils.createContract(
                client,
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                pendingState,
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(15)
        );
        contract2.setId(2L);

        List<Contract> contracts = List.of(contract1, contract2);
        Page<Contract> contractPage = new PageImpl<>(contracts, pageable, contracts.size());

        ContractResponse response1 = new ContractResponse(
                1L, 100.0, "Brand", "Model", "SEDAN", "Class", 2020,
                "Last", contract1.getDataStart(), contract1.getDataEnd(), "VIN123", "GOS123", "PENDING"
        );

        ContractResponse response2 = new ContractResponse(
                2L, 150.0, "Brand", "Model", "SEDAN", "Class", 2020,
                "Last", contract2.getDataStart(), contract2.getDataEnd(), "VIN456", "GOS456", "PENDING"
        );

        given(contractRepository.findByClientId(userId, pageable)).willReturn(contractPage);
        given(contractMapper.toDto(contract1)).willReturn(response1);
        given(contractMapper.toDto(contract2)).willReturn(response2);

        // when
        Page<ContractResponse> actual = serviceUnderTest.getAllClientContracts(pageable, userId);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.getContent()).hasSize(2);
        assertThat(actual.getContent().get(0).id()).isEqualTo(1L);
        assertThat(actual.getContent().get(1).id()).isEqualTo(2L);

        verify(contractRepository).findByClientId(userId, pageable);
    }

    @Test
    @DisplayName("Test get all contracts with filter functionality")
    public void givenFilterRequest_whenGetAllContracts_thenPageIsReturned() {
        // given
        FilterContractRequest filter = new FilterContractRequest("PENDING", null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        RentalState pendingState = dataUtils.getRentalState("PENDING");

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                pendingState,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(1L);

        List<Contract> contracts = List.of(contract);
        Page<Contract> contractPage = new PageImpl<>(contracts, pageable, contracts.size());

        ContractResponse response = new ContractResponse(
                1L, 100.0, "Brand", "Model", "SEDAN", "Class", 2020,
                "Last", contract.getDataStart(), contract.getDataEnd(), "VIN123", "GOS123", "PENDING"
        );

        given(contractRepository.findAllByFilter(
                filter.status(),
                filter.idUser(),
                filter.idCar(),
                filter.brand(),
                filter.bodyType(),
                filter.carClass(),
                pageable
        )).willReturn(contractPage);
        given(contractMapper.toDto(contract)).willReturn(response);

        // when
        Page<ContractResponse> actual = serviceUnderTest.getAllContracts(pageable, filter);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.getContent()).hasSize(1);
        assertThat(actual.getContent().getFirst().state()).isEqualTo("PENDING");

        verify(contractRepository).findAllByFilter(
                filter.status(),
                filter.idUser(),
                filter.idCar(),
                filter.brand(),
                filter.bodyType(),
                filter.carClass(),
                pageable
        );
    }

    @Test
    @DisplayName("Test update contract functionality")
    public void givenUpdateRequest_whenUpdateContract_thenContractIsUpdated() {
        // given
        Long userId = 1L;
        Long contractId = 1L;

        UpdateContractRequest request = new UpdateContractRequest(
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(6)
        );

        Client client = dataUtils.createAndSaveClient("test", "test@test.com");
        client.setId(userId);

        RentalState pendingState = dataUtils.getRentalState("PENDING");

        Car car = dataUtils.getJohnDoePersisted(
                dataUtils.getCarStateTransient("AVAILABLE"),
                dataUtils.getCarModelSEDAN(
                        dataUtils.getBrandTransient(),
                        dataUtils.getModelNameTransient(),
                        dataUtils.getCarClassTransient()
                )
        );


        Contract contract = dataUtils.createContract(
                client,
                car,
                pendingState,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );

        contract.setId(contractId);


        Contract updatedContract = dataUtils.createContract(
                client,
                car,
                pendingState,
                request.dataStart(),
                request.dataEnd()
        );
        updatedContract.setId(contractId);

        ContractResponse response = new ContractResponse(
                1L, 120.0, "Brand", "Model", "SEDAN", "Class", 2020,
                "Last", request.dataStart(), request.dataEnd(), "VIN123", "GOS123", "PENDING"
        );

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));
        given(rentalDomainService.isCarAvailable(request.dataStart(), request.dataEnd(), car.getId())).willReturn(true);


        given(rentalDomainService.calculateCost(
                contract.getCar(),
                contract.getDataStart(),
                contract.getDataEnd()
        )).willReturn(120.0);

        given(contractRepository.save(contract)).willReturn(updatedContract);
        given(contractMapper.toDto(updatedContract)).willReturn(response);

        // when
        ContractResponse actual = serviceUnderTest.updateContract(userId, contractId, request);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(1L);
        assertThat(actual.totalCost()).isEqualTo(120.0);

        verify(contractRepository).findById(contractId);
        verify(rentalDomainService).isCarAvailable(request.dataStart(), request.dataEnd(), car.getId());
        verify(contractMapper).updateContractFromRequest(request, contract);
        verify(contractRepository).save(contract);
    }

    @Test
    @DisplayName("Test update contract by unauthorized user throws exception")
    public void givenUnauthorizedUser_whenUpdateContract_thenThrowException() {
        // given
        Long userId = 1L;
        Long contractId = 1L;

        UpdateContractRequest request = new UpdateContractRequest(
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(6)
        );

        Client client = dataUtils.createAndSaveClient("test", "test@test.com");
        client.setId(2L);

        RentalState pendingState = dataUtils.getRentalState("PENDING");

        Contract contract = dataUtils.createContract(
                client,
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                pendingState,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(contractId);

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));

        // when + then
        assertThrows(
                UnauthorizedContractAccessException.class,
                () -> serviceUnderTest.updateContract(userId, contractId, request)
        );

        verify(contractRepository, never()).save(any(Contract.class));
    }




    @Test
    @DisplayName("Test update contract with invalid date range throws exception")
    public void givenInvalidDateRange_whenUpdateContract_thenThrowException() {
        // given
        Long userId = 1L;
        Long contractId = 1L;

        UpdateContractRequest request = new UpdateContractRequest(
                LocalDate.now().plusDays(6),
                LocalDate.now().plusDays(2)
        );

        // when + then
        assertThrows(
                InvalidContractDateRangeException.class,
                () -> serviceUnderTest.updateContract(userId, contractId, request)
        );

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test update contract with unavailable car throws exception")
    public void givenUnavailableCar_whenUpdateContract_thenThrowException() {
        // given
        Long userId = 1L;
        Long contractId = 1L;

        UpdateContractRequest request = new UpdateContractRequest(
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(6)
        );

        Client client = dataUtils.createAndSaveClient("test", "test@test.com");
        client.setId(userId);

        RentalState pendingState = dataUtils.getRentalState("PENDING");

        Car car = dataUtils.getJohnDoeTransient(
                dataUtils.getCarStateTransient("AVAILABLE"),
                dataUtils.getCarModelSEDAN(
                        dataUtils.getBrandTransient(),
                        dataUtils.getModelNameTransient(),
                        dataUtils.getCarClassTransient()
                )
        );
        car.setId(1L);

        Contract contract = dataUtils.createContract(
                client,
                car,
                pendingState,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(contractId);

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));
        given(rentalDomainService.isCarAvailable(request.dataStart(), request.dataEnd(), car.getId())).willReturn(false);

        // when + then
        assertThrows(
                CarUnavailableOnDatesException.class,
                () -> serviceUnderTest.updateContract(userId, contractId, request)
        );

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test check active contracts by client functionality")
    public void givenClientWithActiveContracts_whenCheckAndAllActiveContractsByClient_thenThrowException() {
        // given
        Client client = dataUtils.createAndSaveClient("test", "test@test.com");
        client.setId(1L);

        RentalState activeState = dataUtils.getRentalState("ACTIVE");

        Contract activeContract = dataUtils.createContract(
                client,
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                activeState,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5)
        );
        activeContract.setId(1L);

        given(contractRepository.findAllByClientAndActiveStates(eq(client), any(Set.class)))
                .willReturn(List.of(activeContract));

        // when + then
        assertThrows(
                BusinessConflictException.class,
                () -> serviceUnderTest.checkAndAllActiveContractsByClient(client)
        );

        verify(contractRepository).findAllByClientAndActiveStates(eq(client), any(Set.class));
    }

    @Test
    @DisplayName("Test check active contracts by client without active contracts")
    public void givenClientWithoutActiveContracts_whenCheckAndAllActiveContractsByClient_thenNoException() {
        // given
        Client client = dataUtils.createAndSaveClient("test", "test@test.com");
        client.setId(1L);

        given(contractRepository.findAllByClientAndActiveStates(eq(client), any(Set.class)))
                .willReturn(List.of());

        // when
        serviceUnderTest.checkAndAllActiveContractsByClient(client);

        // then
        verify(contractRepository).findAllByClientAndActiveStates(eq(client), any(Set.class));
    }

    @Test
    @DisplayName("Test update contract with state change via mapper")
    public void givenUpdateRequest_whenUpdateContract_thenContractDatesAreUpdatedCorrectly() {
        // given
        Long userId = 1L;
        Long contractId = 1L;

        UpdateContractRequest request = new UpdateContractRequest(
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(6)
        );

        Client client = dataUtils.createAndSaveClient("test", "test@test.com");
        client.setId(userId);

        RentalState pendingState = dataUtils.getRentalState("PENDING");

        Car car = dataUtils.getJohnDoeTransient(
                dataUtils.getCarStateTransient("AVAILABLE"),
                dataUtils.getCarModelSEDAN(
                        dataUtils.getBrandTransient(),
                        dataUtils.getModelNameTransient(),
                        dataUtils.getCarClassTransient()
                )
        );
        car.setId(1L);

        Contract contract = dataUtils.createContract(
                client,
                car,
                pendingState,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(contractId);

        ContractResponse response = new ContractResponse(
                1L, 120.0, "Brand", "Model", "SEDAN", "Class", 2020,
                "Last", request.dataStart(), request.dataEnd(), "VIN123", "GOS123", "PENDING"
        );

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));
        given(rentalDomainService.isCarAvailable(request.dataStart(), request.dataEnd(), car.getId())).willReturn(true);
        doAnswer(invocation -> {
            Contract c = invocation.getArgument(1);
            c.setDataStart(request.dataStart());
            c.setDataEnd(request.dataEnd());
            return null;
        }).when(contractMapper).updateContractFromRequest(eq(request), eq(contract));
        given(rentalDomainService.calculateCost(eq(car), any(LocalDate.class), any(LocalDate.class))).willReturn(120.0);
        given(contractRepository.save(contract)).willReturn(contract);
        given(contractMapper.toDto(contract)).willReturn(response);

        // when
        ContractResponse actual = serviceUnderTest.updateContract(userId, contractId, request);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(1L);
        assertThat(actual.totalCost()).isEqualTo(120.0);

        verify(contractMapper).updateContractFromRequest(request, contract);
        verify(rentalDomainService).calculateCost(eq(car), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("Test update contract with active state throws exception")
    public void givenActiveContract_whenUpdateContract_thenThrowException() {
        // given
        Long userId = 1L;
        Long contractId = 1L;

        UpdateContractRequest request = new UpdateContractRequest(
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(6)
        );

        Client client = dataUtils.createAndSaveClient("test", "test@test.com");
        client.setId(userId);

        RentalState activeState = dataUtils.getRentalState("ACTIVE");

        Contract contract = dataUtils.createContract(
                client,
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                activeState,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(contractId);

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));

        // when + then
        assertThrows(
                CannotCancelCompletedContractException.class,
                () -> serviceUnderTest.updateContract(userId, contractId, request)
        );

        verify(contractRepository, never()).save(any(Contract.class));
    }


    @Test
    @DisplayName("Test update contract with confirmed state updates successfully")
    public void givenConfirmedContract_whenUpdateContract_thenUpdateSuccess() {
        // given
        Long userId = 1L;
        Long contractId = 1L;

        UpdateContractRequest request = new UpdateContractRequest(
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(6)
        );

        Client client = dataUtils.createAndSaveClient("test", "test@test.com");
        client.setId(userId);

        RentalState confirmedState = dataUtils.getRentalState("CONFIRMED");

        Contract contract = dataUtils.createContract(
                client,
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                confirmedState,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(contractId);

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));
        given(rentalDomainService.isCarAvailable(any(), any(), any())).willReturn(true);
        given(rentalDomainService.calculateCost(any(), any(), any())).willReturn(500.0);
        given(contractMapper.toDto(any(Contract.class))).willReturn(mock(ContractResponse.class));
        given(contractRepository.save(any(Contract.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        ContractResponse response = serviceUnderTest.updateContract(userId, contractId, request);

        // then
        assertThat(response).isNotNull();
        verify(contractRepository).save(contract);
        verify(rentalDomainService).calculateCost(any(), any(), any());
    }


    @Test
    @DisplayName("Test cancel already cancelled contract does nothing")
    public void givenCancelledContract_whenCancelContract_thenNoActionTaken() {
        // given
        Long userId = 1L;
        Long contractId = 1L;

        Client client = dataUtils.createAndSaveClient("test", "test@test.com");
        client.setId(userId);

        RentalState cancelledState = dataUtils.getRentalState("CANCELLED");

        Contract contract = dataUtils.createContract(
                client,
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                cancelledState,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(contractId);

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));

        // when
        serviceUnderTest.cancelContract(userId, contractId);

        // then
        verify(contractRepository).findById(contractId);
        verify(contractRepository, never()).save(any(Contract.class));
        verify(rentalStateRepository, never()).findByNameIgnoreCase(anyString());
    }

    @Test
    @DisplayName("Test cancel contract by admin with active contract throws exception")
    public void givenActiveContract_whenCancelContractByAdmin_thenThrowException() {
        // given
        Long contractId = 1L;

        RentalState activeState = dataUtils.getRentalState("ACTIVE");

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                activeState,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(contractId);

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));

        // when + then
        assertThrows(
                CannotCancelCompletedContractException.class,
                () -> serviceUnderTest.cancelContractByAdmin(contractId)
        );

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test cancel contract by user with active contract throws CannotCancelCompletedContractException")
    public void givenActiveContract_whenCancelContractByUser_thenThrowsException() {
        // given
        Long userId = 1L;
        Long contractId = 1L;

        Client client = dataUtils.createAndSaveClient("test", "test@test.com");
        client.setId(userId);

        RentalState activeState = dataUtils.getRentalState("ACTIVE");

        Contract contract = dataUtils.createContract(
                client,
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                activeState,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(contractId);

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));

        // when / then
        assertThatThrownBy(() -> serviceUnderTest.cancelContract(userId, contractId))
                .isInstanceOf(CannotCancelCompletedContractException.class)
                .hasMessageContaining("It is not possible to cancel a contract that has the following state:ACTIVE");

        verify(contractRepository).findById(contractId);
        verify(contractRepository, never()).save(any(Contract.class));
    }


    @Test
    @DisplayName("Test cancel contract by admin with confirmed contract")
    public void givenConfirmedContract_whenCancelContractByAdmin_thenContractIsCancelled() {
        // given
        Long contractId = 1L;

        RentalState confirmedState = dataUtils.getRentalState("CONFIRMED");
        RentalState cancelledState = dataUtils.getRentalState("CANCELLED");
        cancelledState.setId(2L);

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                confirmedState,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(contractId);

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));
        given(rentalStateRepository.findByNameIgnoreCase("CANCELLED")).willReturn(Optional.of(cancelledState));
        given(contractRepository.save(contract)).willReturn(contract);

        // when
        serviceUnderTest.cancelContractByAdmin(contractId);

        // then
        verify(contractRepository).findById(contractId);
        verify(rentalStateRepository).findByNameIgnoreCase("CANCELLED");
        verify(contractRepository).save(contract);
    }

    @Test
    @DisplayName("Test cancel contract by admin with cancellation requested contract")
    public void givenCancellationRequestedContract_whenCancelContractByAdmin_thenContractIsCancelled() {
        // given
        Long contractId = 1L;

        RentalState cancellationRequestedState = dataUtils.getRentalState("CANCELLATION_REQUESTED");
        RentalState cancelledState = dataUtils.getRentalState("CANCELLED");
        cancelledState.setId(2L);

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                cancellationRequestedState,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(contractId);

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));
        given(rentalStateRepository.findByNameIgnoreCase("CANCELLED")).willReturn(Optional.of(cancelledState));
        given(contractRepository.save(contract)).willReturn(contract);

        // when
        serviceUnderTest.cancelContractByAdmin(contractId);

        // then
        verify(contractRepository).findById(contractId);
        verify(rentalStateRepository).findByNameIgnoreCase("CANCELLED");
        verify(contractRepository).save(contract);
    }

    @Test
    @DisplayName("Test set active on confirmed contract with start date today")
    public void givenConfirmedContractWithStartToday_whenSetActive_thenContractIsActivated() {
        // given
        RentalState confirmedState = dataUtils.getRentalState("CONFIRMED");
        RentalState activeState = dataUtils.getRentalState("ACTIVE");
        activeState.setId(2L);

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                confirmedState,
                LocalDate.now(),
                LocalDate.now().plusDays(5)
        );
        contract.setId(1L);

        given(rentalStateRepository.findByNameIgnoreCase("ACTIVE")).willReturn(Optional.of(activeState));
        given(contractRepository.save(contract)).willReturn(contract);

        // when
        serviceUnderTest.setActive(contract);

        // then
        verify(rentalStateRepository).findByNameIgnoreCase("ACTIVE");
        verify(contractRepository).save(contract);
    }

    @Test
    @DisplayName("Test set active on confirmed contract with past start date")
    public void givenConfirmedContractWithPastStartDate_whenSetActive_thenContractIsActivated() {
        // given
        RentalState confirmedState = dataUtils.getRentalState("CONFIRMED");
        RentalState activeState = dataUtils.getRentalState("ACTIVE");
        activeState.setId(2L);

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                confirmedState,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(1L);

        given(rentalStateRepository.findByNameIgnoreCase("ACTIVE")).willReturn(Optional.of(activeState));
        given(contractRepository.save(contract)).willReturn(contract);

        // when
        serviceUnderTest.setActive(contract);

        // then
        verify(rentalStateRepository).findByNameIgnoreCase("ACTIVE");
        verify(contractRepository).save(contract);
    }

    @Test
    @DisplayName("Test set active on confirmed contract with future start date does nothing")
    public void givenConfirmedContractWithFutureStartDate_whenSetActive_thenNoActionTaken() {
        // given
        RentalState confirmedState = dataUtils.getRentalState("CONFIRMED");

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                confirmedState,
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(15)
        );
        contract.setId(1L);

        // when
        serviceUnderTest.setActive(contract);

        // then
        verify(rentalStateRepository, never()).findByNameIgnoreCase(anyString());
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test set active on pending contract does nothing")
    public void givenPendingContract_whenSetActive_thenNoActionTaken() {
        // given
        RentalState pendingState = dataUtils.getRentalState("PENDING");

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                pendingState,
                LocalDate.now(),
                LocalDate.now().plusDays(5)
        );
        contract.setId(1L);

        // when
        serviceUnderTest.setActive(contract);

        // then
        verify(rentalStateRepository, never()).findByNameIgnoreCase(anyString());
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test set active on active contract does nothing")
    public void givenActiveContract_whenSetActive_thenNoActionTaken() {
        // given
        RentalState activeState = dataUtils.getRentalState("ACTIVE");

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                activeState,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(1L);

        // when
        serviceUnderTest.setActive(contract);

        // then
        verify(rentalStateRepository, never()).findByNameIgnoreCase(anyString());
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Test activate if due for dto with confirmed contract and start date today")
    public void givenConfirmedContractWithStartToday_whenActivateIfDueForDto_thenDtoHasActiveStatus() {
        // given
        RentalState confirmedState = dataUtils.getRentalState("CONFIRMED");
        RentalState activeState = dataUtils.getRentalState("ACTIVE");
        activeState.setId(2L);

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                confirmedState,
                LocalDate.now(),
                LocalDate.now().plusDays(5)
        );
        contract.setId(1L);

        ContractResponse response = new ContractResponse(
                1L, 100.0, "Brand", "Model", "SEDAN", "Class", 2020,
                "Last", contract.getDataStart(), contract.getDataEnd(), "VIN123", "GOS123", "ACTIVE"
        );

        given(rentalStateRepository.findByNameIgnoreCase("ACTIVE")).willReturn(Optional.of(activeState));
        given(contractMapper.toDto(contract)).willReturn(response);
        given(contractRepository.findByClientId(eq(1L), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(contract)));

        // when
        ContractResponse actual = serviceUnderTest
                .getAllClientContracts(PageRequest.of(0, 10), 1L)
                .getContent().getFirst();

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.state()).isEqualTo("ACTIVE");
    }


    @Test
    @DisplayName("Test activate if due for dto with confirmed contract and past start date")
    public void givenConfirmedContractWithPastStartDate_whenActivateIfDueForDto_thenDtoHasActiveStatus() {
        // given
        RentalState confirmedState = dataUtils.getRentalState("CONFIRMED");
        RentalState activeState = dataUtils.getRentalState("ACTIVE");
        activeState.setId(2L);

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                confirmedState,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5)
        );
        contract.setId(1L);

        ContractResponse response = new ContractResponse(
                1L, 100.0, "Brand", "Model", "SEDAN", "Class", 2020,
                "Last", contract.getDataStart(), contract.getDataEnd(), "VIN123", "GOS123", "ACTIVE"
        );

        given(contractRepository.findByClientId(eq(1L), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(contract)));
        given(rentalStateRepository.findByNameIgnoreCase("ACTIVE")).willReturn(Optional.of(activeState));
        given(contractMapper.toDto(contract)).willReturn(response);

        // when
        Page<ContractResponse> result = serviceUnderTest.getAllClientContracts(PageRequest.of(0, 10), 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().getFirst().state()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Test activate if due for dto with confirmed contract and future start date")
    public void givenConfirmedContractWithFutureStartDate_whenActivateIfDueForDto_thenDtoKeepsConfirmedStatus() {
        // given
        RentalState confirmedState = dataUtils.getRentalState("CONFIRMED");

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                confirmedState,
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(15)
        );
        contract.setId(1L);

        ContractResponse response = new ContractResponse(
                1L, 100.0, "Brand", "Model", "SEDAN", "Class", 2020,
                "Last", contract.getDataStart(), contract.getDataEnd(), "VIN123", "GOS123", "CONFIRMED"
        );

        given(contractRepository.findByClientId(eq(1L), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(contract)));
        given(contractMapper.toDto(contract)).willReturn(response);

        // when
        Page<ContractResponse> result = serviceUnderTest.getAllClientContracts(PageRequest.of(0, 10), 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().getFirst().state()).isEqualTo("CONFIRMED");

        verify(rentalStateRepository, never()).findByNameIgnoreCase("ACTIVE");
    }

    @Test
    @DisplayName("Test activate if due for dto with pending contract")
    public void givenPendingContract_whenActivateIfDueForDto_thenDtoKeepsPendingStatus() {
        // given
        RentalState pendingState = dataUtils.getRentalState("PENDING");

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                pendingState,
                LocalDate.now(),
                LocalDate.now().plusDays(5)
        );
        contract.setId(1L);

        ContractResponse response = new ContractResponse(
                1L, 100.0, "Brand", "Model", "SEDAN", "Class", 2020,
                "Last", contract.getDataStart(), contract.getDataEnd(), "VIN123", "GOS123", "PENDING"
        );

        given(contractRepository.findByClientId(eq(1L), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(contract)));
        given(contractMapper.toDto(contract)).willReturn(response);

        // when
        Page<ContractResponse> result = serviceUnderTest.getAllClientContracts(PageRequest.of(0, 10), 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().getFirst().state()).isEqualTo("PENDING");

        verify(rentalStateRepository, never()).findByNameIgnoreCase("ACTIVE");
    }

    @Test
    @DisplayName("Test activate if due for dto restores original state")
    public void givenConfirmedContract_whenActivateIfDueForDto_thenOriginalStateIsRestored() {
        // given
        RentalState confirmedState = dataUtils.getRentalState("CONFIRMED");
        RentalState activeState = dataUtils.getRentalState("ACTIVE");
        activeState.setId(2L);

        Contract contract = spy(dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                confirmedState,
                LocalDate.now(),
                LocalDate.now().plusDays(5)
        ));
        contract.setId(1L);

        ContractResponse response = new ContractResponse(
                1L, 100.0, "Brand", "Model", "SEDAN", "Class", 2020,
                "Last", contract.getDataStart(), contract.getDataEnd(), "VIN123", "GOS123", "ACTIVE"
        );

        given(contractRepository.findByClientId(eq(1L), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(contract)));
        given(rentalStateRepository.findByNameIgnoreCase("ACTIVE")).willReturn(Optional.of(activeState));
        given(contractMapper.toDto(contract)).willReturn(response);

        // when
        serviceUnderTest.getAllClientContracts(PageRequest.of(0, 10), 1L);

        // then
        // Проверяем, что состояние было установлено дважды: сначала ACTIVE, потом обратно CONFIRMED
        verify(contract, times(2)).setState(any(RentalState.class));
    }


    @Test
    @DisplayName("Test cancelContract does nothing when state is CANCELLED")
    public void givenCancelledContract_whenCancelContractDeclaredMethod_thenNoActionTaken() throws Exception {
        // given
        var method = ContractServiceImpl.class.getDeclaredMethod("cancelContract", Contract.class, boolean.class);
        method.setAccessible(true);

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                dataUtils.getRentalState("CANCELLED"),
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(5)
        );

        // when
        method.invoke(serviceUnderTest, contract, false);

        // then
        verify(contractRepository, never()).save(any(Contract.class));
    }



    @Test
    @DisplayName("Test cancelContract by admin sets CANCELLED when state is CONFIRMED")
    public void givenConfirmedContract_whenCancelByAdmin_thenSetCancelled() throws Exception {
        // given
        var method = ContractServiceImpl.class.getDeclaredMethod("cancelContract", Contract.class, boolean.class);
        method.setAccessible(true);

        RentalState confirmedState = dataUtils.getRentalState("CONFIRMED");
        RentalState cancelledState = dataUtils.getRentalState("CANCELLED");

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("admin", "admin@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                confirmedState,
                LocalDate.now(),
                LocalDate.now().plusDays(2)
        );

        given(rentalStateRepository.findByNameIgnoreCase("CANCELLED"))
                .willReturn(Optional.of(cancelledState));

        // when
        method.invoke(serviceUnderTest, contract, true);

        // then
        assertThat(contract.getState().getName()).isEqualTo("CANCELLED");
        verify(contractRepository).save(contract);
    }


    @Test
    @DisplayName("Test cancelContract by admin throws exception for ACTIVE contract")
    public void givenActiveContract_whenCancelByAdmin_thenThrowException() throws Exception {
        // given
        var method = ContractServiceImpl.class.getDeclaredMethod("cancelContract", Contract.class, boolean.class);
        method.setAccessible(true);

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                dataUtils.getRentalState("ACTIVE"),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(3)
        );

        // then
        assertThrows(
                InvocationTargetException.class,
                () -> method.invoke(serviceUnderTest, contract, true)
        );

        verify(contractRepository, never()).save(any(Contract.class));
    }


    @Test
    @DisplayName("Test cancelContract by user sets CANCELLED when start date more than 5 days ahead")
    public void givenConfirmedContractAndStartFar_whenCancelByUser_thenCancelled() throws Exception {
        // given
        var method = ContractServiceImpl.class.getDeclaredMethod("cancelContract", Contract.class, boolean.class);
        method.setAccessible(true);

        RentalState confirmedState = dataUtils.getRentalState("CONFIRMED");
        RentalState cancelledState = dataUtils.getRentalState("CANCELLED");

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                confirmedState,
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(15)
        );

        given(rentalStateRepository.findByNameIgnoreCase("CANCELLED"))
                .willReturn(Optional.of(cancelledState));

        // when
        method.invoke(serviceUnderTest, contract, false);

        // then
        assertThat(contract.getState().getName()).isEqualTo("CANCELLED");
        verify(contractRepository).save(contract);
    }

    @Test
    @DisplayName("Test cancelContract by user throws exception for ACTIVE contract")
    public void givenActiveContract_whenCancelByUser_thenThrowException() throws Exception {
        // given
        var method = ContractServiceImpl.class.getDeclaredMethod("cancelContract", Contract.class, boolean.class);
        method.setAccessible(true);

        Contract contract = dataUtils.createContract(
                dataUtils.createAndSaveClient("test", "test@test.com"),
                dataUtils.getJohnDoeTransient(
                        dataUtils.getCarStateTransient("AVAILABLE"),
                        dataUtils.getCarModelSEDAN(
                                dataUtils.getBrandTransient(),
                                dataUtils.getModelNameTransient(),
                                dataUtils.getCarClassTransient()
                        )
                ),
                dataUtils.getRentalState("ACTIVE"),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(3)
        );

        // then
        assertThrows(
                InvocationTargetException.class,
                () -> method.invoke(serviceUnderTest, contract, false)
        );

        verify(contractRepository, never()).save(any(Contract.class));
    }

}
