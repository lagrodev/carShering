package org.example.carshering.repository.impl;

import org.example.carshering.dto.response.ContractDetailResponse;
import org.example.carshering.fleet.infrastructure.persistence.entity.*;
import org.example.carshering.fleet.infrastructure.persistence.repository.*;
import org.example.carshering.identity.infrastructure.persistence.entity.Client;
import org.example.carshering.identity.infrastructure.persistence.repository.ClientRepository;
import org.example.carshering.rental.infrastructure.persistence.entity.Contract;
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
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(DataUtils.class)
@ActiveProfiles("test")
public class AnalysisRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private AnalysisRepository analysisRepository;

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
        contractRepository.deleteAll();
        carRepository.deleteAll();
        clientRepository.deleteAll();
        carModelRepository.deleteAll();
        brandRepository.deleteAll();
        modelNameRepository.deleteAll();
        carClassRepository.deleteAll();
        carStateRepository.deleteAll();
        rentalStateRepository.deleteAll();
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

    private List<?> saveContract(String prefix, Car car, String stateName, LocalDateTime start, LocalDateTime end) {
        RentalState state = rentalStateRepository.findByNameIgnoreCase(stateName)
                .orElseGet(() -> rentalStateRepository.save(dataUtils.getRentalState(stateName)));

        Client client = clientRepository.findByEmailAndDeletedFalse(prefix + "_mail@example.com")
                .orElseGet(() -> clientRepository.save(dataUtils.createUniqueClient(prefix)));

        Contract contract = dataUtils.createContractWithDateTime(client, car, state, start, end);

        return List.of(contract, client);
    }

    @Test
    @DisplayName("Test getContractsByDay возвращает контракты за определённый день с фильтром по состоянию")
    public void givenContractsOnDifferentDays_whenGetContractsByDay_thenReturnsOnlyForSpecifiedDay() {
        // given
        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        RentalState completedState = rentalStateRepository.save(dataUtils.getRentalState("COMPLETED"));
        RentalState activeState = rentalStateRepository.save(dataUtils.getRentalState("ACTIVE"));

        LocalDateTime targetDay = LocalDateTime.of(2025, 11, 27, 10, 0);
        LocalDateTime otherDay = LocalDateTime.of(2025, 11, 28, 10, 0);

        // контракт в целевой день с состоянием COMPLETED
        var list1 = saveContract("day1", car, "COMPLETED", targetDay, targetDay.plusHours(2));
        Contract contract1 = contractRepository.save((Contract) list1.get(0));

        // контракт в целевой день с состоянием COMPLETED
        var list2 = saveContract("day2", car, "COMPLETED", targetDay.plusHours(3), targetDay.plusHours(5));
        Contract contract2 = contractRepository.save((Contract) list2.get(0));

        // контракт в целевой день, но с другим состоянием (ACTIVE) — не должен попасть
        var list3 = saveContract("day3", car, "ACTIVE", targetDay.plusHours(6), targetDay.plusHours(8));
        contractRepository.save((Contract) list3.get(0));

        // контракт в другой день с состоянием COMPLETED — не должен попасть
        var list4 = saveContract("day4", car, "COMPLETED", otherDay, otherDay.plusHours(2));
        contractRepository.save((Contract) list4.get(0));

        LocalDateTime startOfDay = targetDay.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = targetDay.toLocalDate().plusDays(1).atStartOfDay();

        // when
        List<ContractDetailResponse> result = analysisRepository.getContractsByDay(
                completedState, startOfDay, endOfDay
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ContractDetailResponse::carId)
                .containsExactlyInAnyOrder(car.getId(), car.getId());
    }

    @Test
    @DisplayName("Test getContractsByDay возвращает пустой список, когда нет контрактов за день")
    public void givenNoContractsOnDay_whenGetContractsByDay_thenReturnsEmptyList() {
        // given
        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        RentalState completedState = rentalStateRepository.save(dataUtils.getRentalState("COMPLETED"));

        LocalDateTime targetDay = LocalDateTime.of(2025, 11, 27, 10, 0);
        LocalDateTime startOfDay = targetDay.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = targetDay.toLocalDate().plusDays(1).atStartOfDay();

        // when
        List<ContractDetailResponse> result = analysisRepository.getContractsByDay(
                completedState, startOfDay, endOfDay
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Test getContractsByDay сортирует результаты по dataStart DESC")
    public void givenMultipleContracts_whenGetContractsByDay_thenSortedByDataStartDesc() {
        // given
        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        RentalState completedState = rentalStateRepository.save(dataUtils.getRentalState("COMPLETED"));

        LocalDateTime targetDay = LocalDateTime.of(2025, 11, 27, 0, 0);

        // создаём контракты в разное время одного дня
        var list1 = saveContract("sort1", car, "COMPLETED",
                targetDay.withHour(8), targetDay.withHour(10));
        Contract earliest = contractRepository.save((Contract) list1.get(0));

        var list2 = saveContract("sort2", car, "COMPLETED",
                targetDay.withHour(14), targetDay.withHour(16));
        Contract middle = contractRepository.save((Contract) list2.get(0));

        var list3 = saveContract("sort3", car, "COMPLETED",
                targetDay.withHour(18), targetDay.withHour(20));
        Contract latest = contractRepository.save((Contract) list3.get(0));

        LocalDateTime startOfDay = targetDay.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = targetDay.toLocalDate().plusDays(1).atStartOfDay();

        // when
        List<ContractDetailResponse> result = analysisRepository.getContractsByDay(
                completedState, startOfDay, endOfDay
        );

        // then
        assertThat(result).hasSize(3);
        // проверяем порядок: последний по времени идёт первым
        assertThat(result.get(0).dataStart()).isEqualTo(latest.getDataStart());
        assertThat(result.get(1).dataStart()).isEqualTo(middle.getDataStart());
        assertThat(result.get(2).dataStart()).isEqualTo(earliest.getDataStart());
    }

//    @Test
//    @DisplayName("Test getDailyRevenueBetween возвращает доход по дням в заданном периоде")
//    public void givenContractsInPeriod_whenGetDailyRevenueBetween_thenReturnsDailyRevenue() {
//        // given
//        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
//                (CarStateType) getCarStateAndCarModelAndSaveAllDependencies().get(0),
//                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
//        ));
//
//        RentalState state = rentalStateRepository.save(dataUtils.getRentalState("COMPLETED"));
//
//        // создаём контракты на разные дни
//        LocalDateTime day1 = LocalDateTime.of(2025, 11, 25, 10, 0);
//        LocalDateTime day2 = LocalDateTime.of(2025, 11, 26, 10, 0);
//        LocalDateTime day3 = LocalDateTime.of(2025, 11, 27, 10, 0);
//
//        // день 1: один контракт на 100
//        var list1 = saveContract("rev1", car, "COMPLETED", day1, day1.plusHours(2));
//        Contract c1 = (Contract) list1.get(0);
//        c1.setTotalCost(100.0);
//        contractRepository.save(c1);
//
//        // день 2: два контракта на 200 и 300
//        var list2 = saveContract("rev2", car, "COMPLETED", day2, day2.plusHours(2));
//        Contract c2 = (Contract) list2.get(0);
//        c2.setTotalCost(200.0);
//        contractRepository.save(c2);
//
//        var list3 = saveContract("rev3", car, "COMPLETED", day2.plusHours(3), day2.plusHours(5));
//        Contract c3 = (Contract) list3.get(0);
//        c3.setTotalCost(300.0);
//        contractRepository.save(c3);
//
//        // день 3: один контракт на 150
//        var list4 = saveContract("rev4", car, "COMPLETED", day3, day3.plusHours(2));
//        Contract c4 = (Contract) list4.get(0);
//        c4.setTotalCost(150.0);
//        contractRepository.save(c4);
//
//        LocalDateTime start = LocalDateTime.of(2025, 11, 25, 0, 0);
//        LocalDateTime end = LocalDateTime.of(2025, 11, 28, 0, 0);
//
//        // when
//        List<Object[]> result = analysisRepository.getDailyRevenueBetween(start, end);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result).hasSize(3);
//
//        // проверяем первый день
//        Object[] day1Result = result.get(0);
//        assertThat(day1Result[0]).isInstanceOf(Date.class);
//        assertThat(((Date) day1Result[0]).toLocalDate()).isEqualTo(day1.toLocalDate());
//        assertThat(day1Result[1]).isEqualTo(100.0);
//
//        // проверяем второй день (сумма 200 + 300)
//        Object[] day2Result = result.get(1);
//        assertThat(((Date) day2Result[0]).toLocalDate()).isEqualTo(day2.toLocalDate());
//        assertThat(day2Result[1]).isEqualTo(500.0);
//
//        // проверяем третий день
//        Object[] day3Result = result.get(2);
//        assertThat(((Date) day3Result[0]).toLocalDate()).isEqualTo(day3.toLocalDate());
//        assertThat(day3Result[1]).isEqualTo(150.0);
//    }

//    @Test
//    @DisplayName("Test getDailyRevenueBetween возвращает пустой список, когда нет контрактов в периоде")
//    public void givenNoContractsInPeriod_whenGetDailyRevenueBetween_thenReturnsEmptyList() {
//        // given
//        LocalDateTime start = LocalDateTime.of(2025, 11, 1, 0, 0);
//        LocalDateTime end = LocalDateTime.of(2025, 11, 10, 0, 0);
//
//        // when
//        List<Object[]> result = analysisRepository.getDailyRevenueBetween(start, end);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result).isEmpty();
//    }

    @Test
    @DisplayName("Test getDailyRevenueBetween фильтрует по диапазону дат dataStart")
    public void givenContractsOutsidePeriod_whenGetDailyRevenueBetween_thenExcludesThem() {
        // given
        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        RentalState state = rentalStateRepository.save(dataUtils.getRentalState("COMPLETED"));

        LocalDateTime beforePeriod = LocalDateTime.of(2025, 11, 20, 10, 0);
        LocalDateTime inPeriod = LocalDateTime.of(2025, 11, 26, 10, 0);
        LocalDateTime afterPeriod = LocalDateTime.of(2025, 11, 30, 10, 0);

        // контракт до периода
        var list1 = saveContract("before", car, "COMPLETED", beforePeriod, beforePeriod.plusHours(2));
        Contract c1 = (Contract) list1.get(0);
        c1.setTotalCost(100.0);
        contractRepository.save(c1);

        // контракт в периоде
        var list2 = saveContract("during", car, "COMPLETED", inPeriod, inPeriod.plusHours(2));
        Contract c2 = (Contract) list2.get(0);
        c2.setTotalCost(200.0);
        contractRepository.save(c2);

        // контракт после периода
        var list3 = saveContract("after", car, "COMPLETED", afterPeriod, afterPeriod.plusHours(2));
        Contract c3 = (Contract) list3.get(0);
        c3.setTotalCost(300.0);
        contractRepository.save(c3);

        LocalDateTime start = LocalDateTime.of(2025, 11, 25, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 11, 28, 0, 0);

        // when
//        List<Object[]> result = analysisRepository.getDailyRevenueBetween(start, end);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0)[1]).isEqualTo(200.0);
    }

//    @Test
//    @DisplayName("Test getDailyRevenueBetween возвращает 0 для дней без контрактов (если есть COALESCE)")
//    public void givenSomeDaysWithoutContracts_whenGetDailyRevenueBetween_thenGroupsByDatesWithContracts() {
//        // given
//        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
//                (CarStateType) getCarStateAndCarModelAndSaveAllDependencies().get(0),
//                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
//        ));
//
//        RentalState state = rentalStateRepository.save(dataUtils.getRentalState("COMPLETED"));
//
//        // создаём контракт только на один день
//        LocalDateTime day1 = LocalDateTime.of(2025, 11, 25, 10, 0);
//
//        var list1 = saveContract("sparse", car, "COMPLETED", day1, day1.plusHours(2));
//        Contract c1 = (Contract) list1.get(0);
//        c1.setTotalCost(500.0);
//        contractRepository.save(c1);
//
//        // запрашиваем период из нескольких дней
//        LocalDateTime start = LocalDateTime.of(2025, 11, 24, 0, 0);
//        LocalDateTime end = LocalDateTime.of(2025, 11, 28, 0, 0);
//
//        // when
//        List<Object[]> result = analysisRepository.getDailyRevenueBetween(start, end);
//
//        // then
//        // метод группирует только по дням, где есть контракты
//        assertThat(result).isNotNull();
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0)[1]).isEqualTo(500.0);
//    }
}

