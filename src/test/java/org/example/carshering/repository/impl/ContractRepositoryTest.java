package org.example.carshering.repository.impl;

import org.example.carshering.fleet.infrastructure.persistence.entity.*;
import org.example.carshering.fleet.infrastructure.persistence.repository.*;
import org.example.carshering.identity.infrastructure.persistence.entity.Client;
import org.example.carshering.identity.infrastructure.persistence.repository.ClientRepository;
import org.example.carshering.rental.infrastructure.persistence.entity.ContractJpaEntity;
import org.example.carshering.rental.infrastructure.persistence.entity.RentalState;
import org.example.carshering.rental.infrastructure.persistence.repository.ContractRepository;
import org.example.carshering.repository.RentalStateRepository;
import org.example.carshering.repository.*;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(DataUtils.class)
@ActiveProfiles("test")
public class ContractRepositoryTest extends AbstractRepositoryTest {


    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private DataUtils dataUtils;
    @Autowired
    private CarModelRepository carModelRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private RentalStateRepository rentalStateRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ModelNameRepository modelNameRepository;
    @Autowired
    private CarClassRepository carClassRepository;
    @Autowired
    private CarStateRepository carStateRepository;

    @BeforeEach
    void setUp() {
        rentalStateRepository.deleteAll();
        modelNameRepository.deleteAll();
        carClassRepository.deleteAll();
        carStateRepository.deleteAll();
        brandRepository.deleteAll();
        carModelRepository.deleteAll();
        clientRepository.deleteAll();
        carRepository.deleteAll();
        contractRepository.deleteAll();
    }

    private List<?> getCarStateAndCarModelAndSaveAllDependencies() {
        Brand brand = brandRepository
                .findByNameIgnoreCase(dataUtils.getBrandTransient().getName())
                .orElseGet(() -> brandRepository.save(dataUtils.getBrandTransient()));

        CarClass carClass = carClassRepository
                .findByNameIgnoreCase(dataUtils.getCarClassTransient().getName())
                .orElseGet(() -> carClassRepository.save(dataUtils.getCarClassTransient()));

        Model modelName = modelNameRepository
                .findByNameIgnoreCase(dataUtils.getModelNameTransient().getName())
                .orElseGet(() -> modelNameRepository.save(dataUtils.getModelNameTransient()));

        CarModel carModel = carModelRepository.save(
                dataUtils.getCarModelSEDAN(brand, modelName, carClass)
        );

        CarState carState = carStateRepository
                .findByStatusIgnoreCase(dataUtils.getCarStateTransient().getStatus())
                .orElseGet(() -> carStateRepository.save(dataUtils.getCarStateTransient()));

        return List.of(carState, carModel);
    }

    private List<?> getCarWithSpecificAttributes(
            String brandStr, String modelNameStr, String carClassStr, String carStateStr, String bodyType) {

        Brand brand = brandRepository
                .findByNameIgnoreCase(brandStr)
                .orElseGet(() -> brandRepository.save(dataUtils.getBrandTransient(brandStr)));

        CarClass carClass = carClassRepository
                .findByNameIgnoreCase(carClassStr)
                .orElseGet(() -> carClassRepository.save(dataUtils.getCarClassTransient(carClassStr)));

        Model modelName = modelNameRepository
                .findByNameIgnoreCase(modelNameStr)
                .orElseGet(() -> modelNameRepository.save(dataUtils.getModelNameTransient(modelNameStr)));

        CarModel carModel = carModelRepository.save(
                dataUtils.getCarModelBody(brand, modelName, carClass, bodyType)
        );

        CarState carState = carStateRepository
                .findByStatusIgnoreCase(carStateStr) // или findByNameIgnoreCase(carStateStr), если status хранится в name
                .orElseGet(() -> carStateRepository.save(dataUtils.getCarStateTransient(carStateStr)));

        return List.of(carState, carModel);
    }


    private List<?> saveContract(String prefix, Car car, String stateName, LocalDateTime start, LocalDateTime end) {
        RentalState state = rentalStateRepository.findByNameIgnoreCase(stateName)
                .orElseGet(() -> rentalStateRepository.save(dataUtils.getRentalState(stateName)));


        Client client = clientRepository.findByEmailAndDeletedFalse(prefix + "_mail@example.com")
                .orElseGet(() -> clientRepository.save(dataUtils.createUniqueClient(prefix)));

        ContractJpaEntity contract = dataUtils.createContractWithDateTime(client, car, state, start, end);

        return List.of(contract, client);

    }

    @Test
    @DisplayName("Test save contract functionality")
    public void givenContractObject_whenSave_thenContractIsCreated() {

        // given
        Car car = carRepository.save(dataUtils.getJohnDoeTransient(

                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        // when
        var list = saveContract("save", car, "BOOKED",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3));


        Client client = (Client) list.get(1);
        ContractJpaEntity saved = contractRepository.save((ContractJpaEntity) list.get(0));


        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getClient().getId()).isEqualTo(client.getId());
        assertThat(saved.getCar().getId()).isEqualTo(car.getId());
    }

    @Test
    @DisplayName("Test update contract functionality")
    public void givenContractToUpdate_whenSave_thenContractIsChanged() {
        // given


        Car car = carRepository.save(dataUtils.getJohnDoeTransient(

                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        var list = saveContract("update", car, "BOOKED",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3));

        ContractJpaEntity saved = contractRepository.save((ContractJpaEntity) list.getFirst());


        // when
        ContractJpaEntity loaded = contractRepository.findById(saved.getId()).orElse(null);
        assertThat(loaded).isNotNull();
        loaded.setComment("updated");
        ContractJpaEntity updated = contractRepository.save(loaded);

        // then
        assertThat(updated.getComment()).isEqualTo("updated");
    }

    @Test
    @DisplayName("findById returns empty when not found")
    public void givenNoContract_whenFindById_thenEmpty() {
        // when
        ContractJpaEntity obtained = contractRepository.findById(999L).orElse(null);

        // then
        assertThat(obtained).isNull();
    }

    @Test
    @DisplayName("findByIdAndUserId returns contract when matches and empty when not")
    public void findByIdAndUserId_checks() {
        // given


        Client other = clientRepository.save( dataUtils.createUniqueClient("other"));


        Car car = carRepository.save(dataUtils.getJohnDoeTransient(

                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        var list = saveContract("iduser", car, "ACTIVE",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        Client client = (Client) list.get(1);
        ContractJpaEntity saved = contractRepository.save((ContractJpaEntity) list.get(0));

        // when
        Optional<ContractJpaEntity> found = contractRepository.findByIdAndUserId(saved.getId(), client.getId());
        Optional<ContractJpaEntity> notFound = contractRepository.findByIdAndUserId(saved.getId(), other.getId());

        // then
        assertThat(found).isPresent();
        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("findByIdAndUserId returns empty for non-existent contract id")
    public void findByIdAndUserId_notExistingContract_returnsEmpty() {
        // given
        Car car = carRepository.save(dataUtils.getJohnDoeTransient(

                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        var list = saveContract("missing", car, "BOOKED", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));


        Client client = (Client)  list.get(1);
        ContractJpaEntity contract = contractRepository.save((ContractJpaEntity) list.get(0));


        // when
        Optional<ContractJpaEntity> result = contractRepository.findByIdAndUserId(99999L, client.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByClientId returns page of contracts for client and empty when none")
    public void findByClientId_checksPaginationAndEmpty() {
        // given
        Car car1 = carRepository.save(dataUtils.getJohnDoeTransient(

                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));
        Car car2 = carRepository.save(dataUtils.getFrankJonesTransient(

                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        var list = saveContract("page", car1, "BOOKED", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        var list2 = saveContract("page", car2, "BOOKED", LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4));

        Client client = (Client) list.get(1);
        contractRepository.save ((ContractJpaEntity) list.get(0));

        contractRepository.save ((ContractJpaEntity) list2.getFirst());

        // when
        Page<ContractJpaEntity> page = contractRepository.findByClientId(client.getId(), pageable);
        Page<ContractJpaEntity> empty = contractRepository.findByClientId(9999L, pageable);

        // then
        assertThat(page.getContent()).hasSize(2);
        assertThat(empty.getContent()).isEmpty();
    }

    @Test
    @DisplayName("deleteById removes contract")
    public void givenContractSaved_whenDeleteById_thenRemoved() {
        // given
        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        var list = saveContract("del", car, "BOOKED",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));


        ContractJpaEntity  saved = contractRepository.save((ContractJpaEntity) list.getFirst());

        // when
        contractRepository.deleteById(saved.getId());

        // then
        ContractJpaEntity obtained = contractRepository.findById(saved.getId()).orElse(null);
        assertThat(obtained).isNull();
    }

    @Test
    @DisplayName("findOverlappingContracts returns only contracts with relevant states, overlapping dates, and excludes given contractId")
    public void findOverlappingContracts_checksWithExcludeId() {
        // given
        var deps = getCarStateAndCarModelAndSaveAllDependencies();
        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
                (CarState) deps.get(0),
                (CarModel) deps.get(1)
        ));

        LocalDate start = LocalDate.of(2025, 1, 10);
        LocalDate end = LocalDate.of(2025, 1, 20);

        // BOOKED, пересекается
        ContractJpaEntity c1 = contractRepository.save((ContractJpaEntity) saveContract("overlap", car, "BOOKED",
                LocalDateTime.of(2025, 1, 5, 0, 0), LocalDateTime.of(2025, 1, 15, 0, 0)).getFirst());

        // CANCELLED, игнорируется по статусу
        ContractJpaEntity c2 = contractRepository.save((ContractJpaEntity) saveContract("overlap", car, "CANCELLED",
                LocalDateTime.of(2025, 1, 12, 0, 0), LocalDateTime.of(2025, 1, 18, 0, 0)).getFirst());

        // ACTIVE, начинается ровно в день окончания диапазона — не пересекается
        contractRepository.save((ContractJpaEntity) saveContract("overlap", car, "ACTIVE",
                LocalDateTime.of(2025, 1, 20, 0, 0), LocalDateTime.of(2025, 1, 25, 0, 0)).getFirst());

        // PENDING, заканчивается ровно в день начала диапазона — не пересекается
        contractRepository.save((ContractJpaEntity) saveContract("overlap", car, "PENDING",
                LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 10, 0, 0)).getFirst());

        // ACTIVE, полностью внутри диапазона — пересекается
        ContractJpaEntity c5 = contractRepository.save((ContractJpaEntity) saveContract("overlap", car, "ACTIVE",
                LocalDateTime.of(2025, 1, 11, 0, 0), LocalDateTime.of(2025, 1, 12, 0, 0)).getFirst());

        // when
        // передаём contractId = null → ничего не исключается
        List<ContractJpaEntity> foundAll = contractRepository.findOverlappingContracts(start.atStartOfDay(), end.atStartOfDay(), car.getId(), null);

        // передаём contractId = c1 → исключаем c1 из выборки
        List<ContractJpaEntity> foundExcludingC1 = contractRepository.findOverlappingContracts(start.atStartOfDay(), end.atStartOfDay(), car.getId(), c1.getId());

        // then
        // без исключения должны вернуться c1 и c5
        assertThat(foundAll)
                .extracting(ContractJpaEntity::getId)
                .containsExactlyInAnyOrder(c1.getId(), c5.getId());

        // при исключении c1 должен остаться только c5
        assertThat(foundExcludingC1)
                .extracting(ContractJpaEntity::getId)
                .containsExactly(c5.getId());

        // проверяем, что CANCELLED не попал
        assertThat(foundAll)
                .noneMatch(c -> c.getId().equals(c2.getId()));

        // проверяем, что граничные случаи не засчитываются
        assertThat(foundAll)
                .allMatch(c -> c.getDataEnd().isAfter(start.atStartOfDay()) && c.getDataStart().isBefore(end.atStartOfDay()));
    }


    @Test
    @DisplayName("findOverlappingContracts boundary cases: touching ranges are not overlaps")
    public void findOverlappingContracts_boundaryCases() {
        // given
        Car car = carRepository.save(dataUtils.getJohnDoeTransient(

                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        LocalDate start = LocalDate.of(2025, 3, 10);
        LocalDate end = LocalDate.of(2025, 3, 20);

        // ends exactly at start -> should NOT overlap
        contractRepository.save ((ContractJpaEntity) saveContract("boundary", car, "BOOKED",
                LocalDateTime.of(2025, 3, 1, 0, 0), LocalDateTime.of(2025, 3, 10, 0, 0)).getFirst());
        // starts exactly at end -> should NOT overlap
        contractRepository.save ((ContractJpaEntity) saveContract("boundary", car, "BOOKED",
                LocalDateTime.of(2025, 3, 20, 0, 0), LocalDateTime.of(2025, 3, 25, 0, 0)).getFirst());
        // fully inside -> should overlap
        ContractJpaEntity inside = contractRepository.save ((ContractJpaEntity) saveContract("boundary", car, "ACTIVE",
                LocalDateTime.of(2025, 3, 12, 0, 0), LocalDateTime.of(2025, 3, 15, 0, 0)).getFirst());
        // covering entire range -> should overlap
        ContractJpaEntity covering =contractRepository.save ((ContractJpaEntity) saveContract("boundary", car, "ACTIVE",
                LocalDateTime.of(2025, 3, 5, 0, 0), LocalDateTime.of(2025, 3, 25, 0, 0)).getFirst());
        // exact match -> should overlap
        ContractJpaEntity exact = contractRepository.save ((ContractJpaEntity) saveContract("boundary", car, "BOOKED",
                LocalDateTime.of(2025, 3, 10, 0, 0), LocalDateTime.of(2025, 3, 20, 0, 0)).getFirst());

        // when
        List<ContractJpaEntity> found = contractRepository.findOverlappingContracts(start.atStartOfDay(), end.atStartOfDay(), car.getId(), null);

        // then
        assertThat(found).extracting(ContractJpaEntity::getId)
                .containsExactlyInAnyOrder(inside.getId(), covering.getId(), exact.getId());
    }

    @Test
    @DisplayName("findOverlappingContracts returns empty when no overlaps")
    public void findOverlappingContracts_noOverlaps() {
        // given
        Car car = carRepository.save(dataUtils.getJohnDoeTransient(

                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        LocalDateTime start = LocalDateTime.of(2025, 2, 1,1,1);
        LocalDateTime end = LocalDateTime.of(2025, 2, 10, 1,1);

        contractRepository.save ((ContractJpaEntity) saveContract("nooverlap", car, "BOOKED",
                LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 5, 0, 0)).getFirst());
        contractRepository.save ((ContractJpaEntity) saveContract("nooverlap", car, "ACTIVE",
                LocalDateTime.of(2025, 2, 10, 0, 0), LocalDateTime.of(2025, 2, 15, 0, 0)).getFirst()); // starts exactly at end -> should not overlap

        // when
        List<ContractJpaEntity> found = contractRepository.findOverlappingContracts(start, end, car.getId(), null);

        // then
        assertThat(found).isEmpty();
    }


    private final Pageable pageable = PageRequest.of(0, 10);

    @Test
    @DisplayName("findAllByFilter filters correctly by status, user, car, bodyType and carClass and handles nulls")
    public void findAllByFilter_checksAndEdgeCases() {
        // prepare


        // Build cars using entity-resolving helper
        var carAList = getCarWithSpecificAttributes("BRANDX", "MODELX", "CLASSA", "ACTIVE", "SEDAN");





        Car carA = carRepository.save(dataUtils.getCarWithSpecificAttributes(
                "VIN_A", "G_A", 2020,
                (CarState) carAList.get(0),
                (CarModel) carAList.get(1)));

        var carBList = getCarWithSpecificAttributes("BRANDY", "MODELY", "CLASSB", "ACTIVE", "SUV");

        Car carB = carRepository.save(dataUtils.getCarWithSpecificAttributes(
                "VIN_B", "G_B", 2021,
                (CarState) carBList.get(0),
                (CarModel) carBList.get(1)));

        // ContractJpaEntity status is likely a String field, so keep "ACTIVE", "BOOKED" as literals


        var list = saveContract("fA", carA, "ACTIVE", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));


        var list2 = saveContract("fA", carB, "BOOKED", LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4));

        var list3 = saveContract("fB", carA, "ACTIVE", LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(6));


        ContractJpaEntity ca1 = contractRepository.save ((ContractJpaEntity) list .getFirst());

        ContractJpaEntity ca2 = contractRepository.save ((ContractJpaEntity) list2 .getFirst());

        ContractJpaEntity ca3 = contractRepository.save ((ContractJpaEntity) list3.getFirst());
        // После сохранения всех контрактов
        // После сохранения всех контрактов
        List<ContractJpaEntity> all2 = contractRepository.findAll();
        all2.forEach(c -> System.out.println("ID: " + c.getId() + "State:" + c.getState()));


        // when - filter by status

        Page<ContractJpaEntity> byStatus = contractRepository.findAllByFilter
                ("ACTIVE", null, null, null, null, null, pageable);
        assertThat(byStatus.getContent()).hasSize(2);

        Client clientA = (Client) list.get(1);
        // when - filter by user
        Page<ContractJpaEntity> byUser = contractRepository.findAllByFilter(null, clientA.getId(), null, null, null, null, pageable);
        assertThat(byUser.getContent()).hasSize(2);

        // when - filter by car id
        Page<ContractJpaEntity> byCar = contractRepository.findAllByFilter(null, null, carB.getId(), null, null, null, pageable);
        assertThat(byCar.getContent()).hasSize(1);
        assertThat(byCar.getContent().get(0).getId()).isEqualTo(ca2.getId());

        // when - filter by bodyType
        Page<ContractJpaEntity> byBody = contractRepository.findAllByFilter(null, null, null, null, "SEDAN", null, pageable);
        assertThat(byBody.getContent()).hasSize(2); // ca1 and cb1 use carA with SEDAN

        // when - filter by carClass
        Page<ContractJpaEntity> byClass = contractRepository.findAllByFilter(null, null, null, null, null, "CLASSB", pageable);
        assertThat(byClass.getContent()).hasSize(1);
        assertThat(byClass.getContent().get(0).getId()).isEqualTo(ca2.getId());

        // when - combined filter: status and user
        Page<ContractJpaEntity> combined = contractRepository.findAllByFilter("ACTIVE", clientA.getId(), null, null, null, null, pageable);
        assertThat(combined.getContent()).hasSize(1);
        assertThat(combined.getContent().get(0).getId()).isEqualTo(ca1.getId());

        // when - all params null should return all
        Page<ContractJpaEntity> all = contractRepository.findAllByFilter(null, null, null, null, null, null, pageable);
        assertThat(all.getContent()).hasSize(3);

        // when - no matches
        Page<ContractJpaEntity> nomatch = contractRepository.findAllByFilter("NONEXISTENT", null, null, null, null, null, pageable);
        assertThat(nomatch.getContent()).isEmpty();
    }




    @Test
    @DisplayName("findAllByFilter respects pagination")
    public void findAllByFilter_pagination() {
        // given
        Car car = carRepository.save(dataUtils.getJohnDoeTransient(

                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        for (int i = 0; i < 15; i++) {
            contractRepository.save ((ContractJpaEntity)saveContract(
                    "pg", car, "BOOKED", LocalDateTime.now().plusDays(i + 1), LocalDateTime.now().plusDays(i + 2)).getFirst());
        }

        // when
        Page<ContractJpaEntity> first = contractRepository.findAllByFilter(null, null, null, null, null, null, pageable);
        Page<ContractJpaEntity> second = contractRepository.findAllByFilter(null, null, null, null, null, null, PageRequest.of(1, 10));

        // then
        assertThat(first.getContent()).hasSize(10);
        assertThat(second.getContent()).hasSize(5);
    }


    @Test
    @DisplayName("findAllByClientAndActiveStates returns only contracts matching client and active states (case-insensitive, uppercase comparison)")
    void findAllByClientAndActiveStates_returnsOnlyMatchingActiveContracts() {
        // given
        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        // Сохраняем клиента один раз, чтобы все контракты принадлежали одному пользователю
        var list = saveContract("active_filter", car, "ACTIVE",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        Client client = (Client) list.get(1);

        // Создаём несколько контрактов с разными состояниями
        ContractJpaEntity active = contractRepository.save((ContractJpaEntity) saveContract("active_filter", car, "ACTIVE",
                LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4)).getFirst());
        ContractJpaEntity booked = contractRepository.save((ContractJpaEntity) saveContract("active_filter", car, "BOOKED",
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(6)).getFirst());
        ContractJpaEntity pending = contractRepository.save((ContractJpaEntity) saveContract("active_filter", car, "PENDING",
                LocalDateTime.now().plusDays(7), LocalDateTime.now().plusDays(8)).getFirst());

        // Этот контракт не должен попасть в выборку — состояние отменено
        contractRepository.save((ContractJpaEntity) saveContract("active_filter", car, "CANCELLED",
                LocalDateTime.now().plusDays(9), LocalDateTime.now().plusDays(10)).getFirst());

        // Другой клиент — его контракт тоже не должен попасть
        Client otherClient = clientRepository.save(dataUtils.createUniqueClient("other_for_active_filter"));
        ContractJpaEntity otherActive = dataUtils.createContract(otherClient, car,
                rentalStateRepository.findByNameIgnoreCase("ACTIVE")
                        .orElseGet(() -> rentalStateRepository.save(dataUtils.getRentalState("ACTIVE"))),
                LocalDate.now().plusDays(11), LocalDate.now().plusDays(12));
        contractRepository.save(otherActive);

        // when
        // Передаём состояния в верхнем регистре — как ожидает запрос (UPPER(c.state) IN :activeStates)
        List<ContractJpaEntity> resultList = contractRepository.findAllByClientAndActiveStates(
                client,
                List.of("ACTIVE", "BOOKED", "PENDING") // все активные состояния
        );

        // then

        assertThat(resultList)
                .hasSize(3)
                .extracting(ContractJpaEntity::getId)
                .containsExactlyInAnyOrder(active.getId(), booked.getId(), pending.getId());

        // Убеждаемся, что отменённый и чужой контракты не попали
        assertThat(resultList)
                .noneMatch(c -> c.getId().equals(otherActive.getId()))
                .noneMatch(c -> "CANCELLED".equalsIgnoreCase(c.getState().getName()));
    }

    @Test
    @DisplayName("findAllByClientAndActiveStates returns empty when no active contracts exist for client")
    void findAllByClientAndActiveStates_noActiveContracts_returnsEmpty() {
        // given
        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        var list = saveContract("no_active", car, "CANCELLED",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        Client client = (Client) list.get(1);
        contractRepository.save((ContractJpaEntity) list.getFirst());

        // when
        List <ContractJpaEntity> resultList = contractRepository.findAllByClientAndActiveStates(
                client,
                List.of("ACTIVE", "BOOKED")
        );

        // then

        assertThat(resultList).isEmpty();
    }

    @Test
    @DisplayName("findAllByClientAndActiveStates handles empty activeStates collection gracefully")
    void findAllByClientAndActiveStates_emptyActiveStates_returnsEmpty() {
        // given
        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        var list = saveContract("empty_states", car, "ACTIVE",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        Client client = (Client) list.get(1);
        contractRepository.save((ContractJpaEntity) list.getFirst());

        // when
        List<ContractJpaEntity> resultList = contractRepository.findAllByClientAndActiveStates(client, List.of());

        // then
        assertThat(resultList).isEmpty();
    }


    /**
     * Проверяет устойчивость метода {@link ContractRepository#findAllByFilter} к SQL-инъекциям
     * через все строковые параметры: {@code status}, {@code brand}, {@code bodyType}, {@code carClass}.
     * <p>
     * Метод использует JPQL с параметризованными выражениями ({@code :param}), но поскольку
     * параметры поступают от пользователя как строки, необходимо убедиться, что:
     * <ul>
     *   <li>Злонамеренные значения (включая UNION, DROP, комментарии) интерпретируются как литералы;</li>
     *   <li>Не происходит утечки данных или выполнения произвольного кода;</li>
     *   <li>Легитимные фильтры продолжают работать корректно.</li>
     * </ul>
     * <p>
     * Почему НЕ тестируются остальные методы репозитория:
     * <ul>
     *   <li>{@code findOverlappingContracts} — принимает только {@code LocalDate} и {@code Long}.
     *       Оба типа не могут содержать SQL-синтаксис; Spring парсит их до попадания в запрос.</li>
     *   <li>{@code findByIdAndUserId} — оба параметра типа {@code Long}. Инъекция невозможна:
     *       даже при передаче из HTTP, невалидная строка вызовет {@code TypeMismatchException}
     *       на уровне контроллера или конвертера, а не в репозитории.</li>
     *   <li>{@code findByClientId} — параметр {@code Long userId}, аналогично безопасен.</li>
     * </ul>
     * <p>
     * Вывод: только методы с {@code String}-параметрами, зависящими от пользовательского ввода,
     * подвержены риску SQL-инъекции и требуют явной валидации.
     */
    @Test
    @DisplayName("findAllByFilter is safe against SQL injection in string parameters (status, brand, bodyType, carClass)")
    void findAllByFilter_sqlInjectionAttempts_returnEmptyAndDoNotExposeData() {
        // given

        var carAList = getCarWithSpecificAttributes
                ("SAFE_BRAND", "SAFE_MODEL", "LUXURY", "ACTIVE", "SEDAN");


        Car car = carRepository.save(dataUtils.getCarWithSpecificAttributes(
                "VIN_INJ", "G_INJ", 2020,
                (CarState) carAList.get(0),
                (CarModel) carAList.get(1)));


        var list = saveContract("id", car, "ACTIVE",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        ContractJpaEntity legitimateContract = contractRepository.save((ContractJpaEntity) list.getFirst());

        List<String> injectionPayloads = Arrays.asList(
                "' OR '1'='1",
                "'; DROP TABLE contracts; --",
                "ACTIVE'--",
                "ACTIVE' OR 'x'='x",
                "\" OR \"\"=\"",
                "*/ UNION SELECT * FROM contracts; /*",
                "\\'; DELETE FROM contracts; --",
                "x' AND 1=1; --",
                "/*",
                "1' UNION SELECT null--"
        );

        // when & then
        for (String payload : injectionPayloads) {
            // Test each vulnerable string parameter individually

            // 1. status
            Page<ContractJpaEntity> byStatus = contractRepository.findAllByFilter(
                    payload, null, null, null, null, null, pageable);
            assertThat(byStatus.getContent())
                    .as("findAllByFilter should return empty for status payload: %s", payload)
                    .isEmpty();

            // 2. brand
            Page<ContractJpaEntity> byBrand = contractRepository.findAllByFilter(
                    null, null, null, payload, null, null, pageable);
            assertThat(byBrand.getContent())
                    .as("findAllByFilter should return empty for brand payload: %s", payload)
                    .isEmpty();

            // 3. bodyType
            Page<ContractJpaEntity> byBodyType = contractRepository.findAllByFilter(
                    null, null, null, null, payload, null, pageable);
            assertThat(byBodyType.getContent())
                    .as("findAllByFilter should return empty for bodyType payload: %s", payload)
                    .isEmpty();

            // 4. carClass
            Page<ContractJpaEntity> byCarClass = contractRepository.findAllByFilter(
                    null, null, null, null, null, payload, pageable);
            assertThat(byCarClass.getContent())
                    .as("findAllByFilter should return empty for carClass payload: %s", payload)
                    .isEmpty();
        }

        // Убедимся, что легитимные значения всё ещё работают
        Page<ContractJpaEntity> validStatus = contractRepository.findAllByFilter("ACTIVE", null, null, null, null, null, pageable);
        assertThat(validStatus.getContent()).contains(legitimateContract);

        Page<ContractJpaEntity> validBrand = contractRepository.findAllByFilter(null, null, null, "SAFE_BRAND", null, null, pageable);
        assertThat(validBrand.getContent()).contains(legitimateContract);

        Page<ContractJpaEntity> validBody = contractRepository.findAllByFilter(null, null, null, null, "SEDAN", null, pageable);
        assertThat(validBody.getContent()).contains(legitimateContract);

        Page<ContractJpaEntity> validClass = contractRepository.findAllByFilter(null, null, null, null, null, "LUXURY", pageable);
        assertThat(validClass.getContent()).contains(legitimateContract);
    }




    @Test
    @DisplayName("findAllByClientAndActiveStates is safe against SQL injection in activeStates parameter")
    void findAllByClientAndActiveStates_sqlInjectionAttempts_returnEmptyAndDoNotExposeData() {
        // given
        // Создаём тестовые сущности
        var carList = getCarWithSpecificAttributes(
                "SAFE_BRAND2", "SAFE_MODEL2", "MID", "ACTIVE", "SEDAN"
        );

        Car car = carRepository.save(dataUtils.getCarWithSpecificAttributes(
                "VIN_SQLINJ", "G_SQLINJ", 2022,
                (CarState) carList.get(0),
                (CarModel) carList.get(1)
        ));

        // создаём клиента и контракт в легитимном состоянии ACTIVE
        var list = saveContract("sqlinj", car, "ACTIVE",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3));

        Client client = (Client) list.get(1);
        ContractJpaEntity legit = contractRepository.save((ContractJpaEntity) list.get(0));

        // список инъекционных нагрузок для проверки устойчивости метода
        List<String> injectionPayloads = Arrays.asList(
                "' OR '1'='1",
                "'; DROP TABLE contracts; --",
                "ACTIVE'--",
                "ACTIVE' OR 'x'='x",
                "\" OR \"\"=\"",
                "*/ UNION SELECT * FROM contracts; /*",
                "\\'; DELETE FROM contracts; --",
                "x' AND 1=1; --",
                "/*",
                "1' UNION SELECT null--"
        );

        // when & then
        for (String payload : injectionPayloads) {
            Iterable<ContractJpaEntity> result = contractRepository.findAllByClientAndActiveStates(
                    client,
                    List.of(payload)
            );

            // Метод должен вернуть пустой результат
            assertThat(result)
                    .as("findAllByClientAndActiveStates should return empty for payload: %s", payload)
                    .isEmpty();
        }

        // Проверяем, что легитимное значение всё ещё возвращает ожидаемый результат
        List<ContractJpaEntity> validResult = contractRepository.findAllByClientAndActiveStates(
                client,
                List.of("ACTIVE")
        );

        assertThat(validResult)
                .as("findAllByClientAndActiveStates should return legitimate contracts for 'ACTIVE'")
                .extracting(ContractJpaEntity::getId)
                .containsExactly(legit.getId());

        // и что инъекционные вызовы не повредили данные
        assertThat(contractRepository.findAll()).contains(legit);
    }


}
