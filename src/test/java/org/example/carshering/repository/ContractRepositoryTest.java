package org.example.carshering.repository;

import org.example.carshering.entity.Car;
import org.example.carshering.entity.Client;
import org.example.carshering.entity.Contract;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(DataUtils.class)
@ActiveProfiles("test")
public class ContractRepositoryTest extends AbstractRepositoryTest {

    private final Pageable pageable = PageRequest.of(0, 10);

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private DataUtils dataUtils;

    @BeforeEach
    void setUp() {
        contractRepository.deleteAll();
        carRepository.deleteAll();
    }

    @Test
    @DisplayName("Test save contract functionality")
    public void givenContractObject_whenSave_thenContractIsCreated() {
        // given
        Client client = dataUtils.createUniqueClient("save");
        Car car = carRepository.save(dataUtils.getJohnDoeTransient());

        // when
        Contract saved = dataUtils.createContract(client, car, "BOOKED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

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
        Client client = dataUtils.createUniqueClient("update");
        Car car = carRepository.save(dataUtils.getJohnDoeTransient());
        Contract saved = dataUtils.createContract(client, car, "BOOKED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));

        // when
        Contract loaded = contractRepository.findById(saved.getId()).orElse(null);
        assertThat(loaded).isNotNull();
        loaded.setComment("updated");
        Contract updated = contractRepository.save(loaded);

        // then
        assertThat(updated.getComment()).isEqualTo("updated");
    }

    @Test
    @DisplayName("findById returns empty when not found")
    public void givenNoContract_whenFindById_thenEmpty() {
        // when
        Contract obtained = contractRepository.findById(999L).orElse(null);

        // then
        assertThat(obtained).isNull();
    }

    @Test
    @DisplayName("findByIdAndUserId returns contract when matches and empty when not")
    public void findByIdAndUserId_checks() {
        // given
        Client client = dataUtils.createUniqueClient("iduser");
        Client other = dataUtils.createUniqueClient("other");
        Car car = carRepository.save(dataUtils.getJohnDoeTransient());

        Contract c = dataUtils.createContract(client, car, "ACTIVE",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));

        // when
        Optional<Contract> found = contractRepository.findByIdAndUserId(c.getId(), client.getId());
        Optional<Contract> notFound = contractRepository.findByIdAndUserId(c.getId(), other.getId());

        // then
        assertThat(found).isPresent();
        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("findByIdAndUserId returns empty for non-existent contract id")
    public void findByIdAndUserId_notExistingContract_returnsEmpty() {
        // given
        Client client = dataUtils.createUniqueClient("missing");
        Car car = carRepository.save(dataUtils.getJohnDoeTransient());
        dataUtils.createContract(client, car, "BOOKED", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));

        // when
        Optional<Contract> result = contractRepository.findByIdAndUserId(99999L, client.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByClientId returns page of contracts for client and empty when none")
    public void findByClientId_checksPaginationAndEmpty() {
        // given
        Client client = dataUtils.createUniqueClient("page");
        Car car1 = carRepository.save(dataUtils.getJohnDoeTransient());
        Car car2 = carRepository.save(dataUtils.getFrankJonesTransient());

        dataUtils.createContract(client, car1, "BOOKED", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        dataUtils.createContract(client, car2, "BOOKED", LocalDate.now().plusDays(3), LocalDate.now().plusDays(4));

        // when
        Page<Contract> page = contractRepository.findByClientId(client.getId(), pageable);
        Page<Contract> empty = contractRepository.findByClientId(9999L, pageable);

        // then
        assertThat(page.getContent()).hasSize(2);
        assertThat(empty.getContent()).isEmpty();
    }

    @Test
    @DisplayName("deleteById removes contract")
    public void givenContractSaved_whenDeleteById_thenRemoved() {
        // given
        Client client = dataUtils.createUniqueClient("del");
        Car car = carRepository.save(dataUtils.getJohnDoeTransient());
        Contract saved = dataUtils.createContract(client, car, "BOOKED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));

        // when
        contractRepository.deleteById(saved.getId());

        // then
        Contract obtained = contractRepository.findById(saved.getId()).orElse(null);
        assertThat(obtained).isNull();
    }

    @Test
    @DisplayName("findOverlappingContracts returns only contracts with relevant states and overlapping dates")
    public void findOverlappingContracts_checks() {
        // given
        Client client = dataUtils.createUniqueClient("overlap");
        Car car = carRepository.save(dataUtils.getJohnDoeTransient());

        LocalDate start = LocalDate.of(2025, 1, 10);
        LocalDate end = LocalDate.of(2025, 1, 20);

        Contract c1 = dataUtils.createContract(client, car, "BOOKED",
                LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 15));
        Contract c2 = dataUtils.createContract(client, car, "CANCELLED",
                LocalDate.of(2025, 1, 12), LocalDate.of(2025, 1, 18));
        dataUtils.createContract(client, car, "ACTIVE",
                LocalDate.of(2025, 1, 20), LocalDate.of(2025, 1, 25));
        dataUtils.createContract(client, car, "PENDING",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 10));
        Contract c5 = dataUtils.createContract(client, car, "ACTIVE",
                LocalDate.of(2025, 1, 11), LocalDate.of(2025, 1, 12));

        // when
        List<Contract> found = contractRepository.findOverlappingContracts(start, end, car.getId());

        // then
        // c1 and c5 should be returned (states BOOKED/ACTIVE/PENDING are considered), c2 is CANCELLED and ignored, other edge ones do not overlap
        assertThat(found).extracting(Contract::getId).containsExactlyInAnyOrder(c1.getId(), c5.getId());
        assertThat(found).noneMatch(ct -> ct.getId().equals(c2.getId()));
    }

    @Test
    @DisplayName("findOverlappingContracts boundary cases: touching ranges are not overlaps")
    public void findOverlappingContracts_boundaryCases() {
        // given
        Client client = dataUtils.createUniqueClient("boundary");
        Car car = carRepository.save(dataUtils.getJohnDoeTransient());

        LocalDate start = LocalDate.of(2025, 3, 10);
        LocalDate end = LocalDate.of(2025, 3, 20);

        // ends exactly at start -> should NOT overlap
        dataUtils.createContract(client, car, "BOOKED",
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 10));
        // starts exactly at end -> should NOT overlap
        dataUtils.createContract(client, car, "BOOKED",
                LocalDate.of(2025, 3, 20), LocalDate.of(2025, 3, 25));
        // fully inside -> should overlap
        Contract inside = dataUtils.createContract(client, car, "ACTIVE",
                LocalDate.of(2025, 3, 12), LocalDate.of(2025, 3, 15));
        // covering entire range -> should overlap
        Contract covering = dataUtils.createContract(client, car, "ACTIVE",
                LocalDate.of(2025, 3, 5), LocalDate.of(2025, 3, 25));
        // exact match -> should overlap
        Contract exact = dataUtils.createContract(client, car, "BOOKED",
                LocalDate.of(2025, 3, 10), LocalDate.of(2025, 3, 20));

        // when
        List<Contract> found = contractRepository.findOverlappingContracts(start, end, car.getId());

        // then
        assertThat(found).extracting(Contract::getId)
                .containsExactlyInAnyOrder(inside.getId(), covering.getId(), exact.getId());
    }

    @Test
    @DisplayName("findOverlappingContracts returns empty when no overlaps")
    public void findOverlappingContracts_noOverlaps() {
        // given
        Client client = dataUtils.createUniqueClient("nooverlap");
        Car car = carRepository.save(dataUtils.getJohnDoeTransient());

        LocalDate start = LocalDate.of(2025, 2, 1);
        LocalDate end = LocalDate.of(2025, 2, 10);

        dataUtils.createContract(client, car, "BOOKED",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5));
        dataUtils.createContract(client, car, "ACTIVE",
                LocalDate.of(2025, 2, 10), LocalDate.of(2025, 2, 15)); // starts exactly at end -> should not overlap

        // when
        List<Contract> found = contractRepository.findOverlappingContracts(start, end, car.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findAllByFilter filters correctly by status, user, car, bodyType and carClass and handles nulls")
    public void findAllByFilter_checksAndEdgeCases() {
        // prepare
        Client clientA = dataUtils.createUniqueClient("fA");
        Client clientB = dataUtils.createUniqueClient("fB");

        Car carA = carRepository.save(dataUtils.getCarWithSpecificAttributes(
                "VIN_A", "G_A", 2020, "BRANDX", "MODELX", "SEDAN", "CLASSA", "ACTIVE"));
        Car carB = carRepository.save(dataUtils.getCarWithSpecificAttributes(
                "VIN_B", "G_B", 2021, "BRANDY", "MODELY", "SUV", "CLASSB", "ACTIVE"));

        Contract ca1 = dataUtils.createContract(clientA, carA, "ACTIVE", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        Contract ca2 = dataUtils.createContract(clientA, carB, "BOOKED", LocalDate.now().plusDays(3), LocalDate.now().plusDays(4));
        dataUtils.createContract(clientB, carA, "ACTIVE", LocalDate.now().plusDays(5), LocalDate.now().plusDays(6));

        // when - filter by status
        Page<Contract> byStatus = contractRepository.findAllByFilter("ACTIVE", null, null, null, null, null, pageable);
        // then
        assertThat(byStatus.getContent()).hasSize(2);

        // when - filter by user
        Page<Contract> byUser = contractRepository.findAllByFilter(null, clientA.getId(), null, null, null, null, pageable);
        // then
        assertThat(byUser.getContent()).hasSize(2);

        // when - filter by car id
        Page<Contract> byCar = contractRepository.findAllByFilter(null, null, carB.getId(), null, null, null, pageable);
        // then
        assertThat(byCar.getContent()).hasSize(1);
        assertThat(byCar.getContent().get(0).getId()).isEqualTo(ca2.getId());

        // when - filter by bodyType
        Page<Contract> byBody = contractRepository.findAllByFilter(null, null, null, null, "SEDAN", null, pageable);
        // then
        assertThat(byBody.getContent()).hasSize(2); // ca1 and cb1 use carA with SEDAN

        // when - filter by carClass
        Page<Contract> byClass = contractRepository.findAllByFilter(null, null, null, null, null, "CLASSB", pageable);
        // then
        assertThat(byClass.getContent()).hasSize(1);
        assertThat(byClass.getContent().get(0).getId()).isEqualTo(ca2.getId());

        // when - combined filter: status and user
        Page<Contract> combined = contractRepository.findAllByFilter("ACTIVE", clientA.getId(), null, null, null, null, pageable);
        // then
        assertThat(combined.getContent()).hasSize(1);
        assertThat(combined.getContent().get(0).getId()).isEqualTo(ca1.getId());

        // when - all params null should return all
        Page<Contract> all = contractRepository.findAllByFilter(null, null, null, null, null, null, pageable);
        // then
        assertThat(all.getContent()).hasSize(3);

        // when - no matches
        Page<Contract> nomatch = contractRepository.findAllByFilter("NONEXISTENT", null, null, null, null, null, pageable);
        // then
        assertThat(nomatch.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findAllByFilter respects pagination")
    public void findAllByFilter_pagination() {
        // given
        Client client = dataUtils.createUniqueClient("pg");
        Car car = carRepository.save(dataUtils.getJohnDoeTransient());

        for (int i = 0; i < 15; i++) {
            dataUtils.createContract(client, car, "BOOKED", LocalDate.now().plusDays(i + 1), LocalDate.now().plusDays(i + 2));
        }

        // when
        Page<Contract> first = contractRepository.findAllByFilter(null, null, null, null, null, null, pageable);
        Page<Contract> second = contractRepository.findAllByFilter(null, null, null, null, null, null, PageRequest.of(1, 10));

        // then
        assertThat(first.getContent()).hasSize(10);
        assertThat(second.getContent()).hasSize(5);
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
        Client client = dataUtils.createUniqueClient("inj");
        Car car = carRepository.save(dataUtils.getCarWithSpecificAttributes(
                "VIN_INJ", "G_INJ", 2020, "SAFE_BRAND", "SAFE_MODEL", "SEDAN", "LUXURY", "ACTIVE"));
        Contract legitimateContract = dataUtils.createContract(client, car, "ACTIVE",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));

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
            Page<Contract> byStatus = contractRepository.findAllByFilter(
                    payload, null, null, null, null, null, pageable);
            assertThat(byStatus.getContent())
                    .as("findAllByFilter should return empty for status payload: %s", payload)
                    .isEmpty();

            // 2. brand
            Page<Contract> byBrand = contractRepository.findAllByFilter(
                    null, null, null, payload, null, null, pageable);
            assertThat(byBrand.getContent())
                    .as("findAllByFilter should return empty for brand payload: %s", payload)
                    .isEmpty();

            // 3. bodyType
            Page<Contract> byBodyType = contractRepository.findAllByFilter(
                    null, null, null, null, payload, null, pageable);
            assertThat(byBodyType.getContent())
                    .as("findAllByFilter should return empty for bodyType payload: %s", payload)
                    .isEmpty();

            // 4. carClass
            Page<Contract> byCarClass = contractRepository.findAllByFilter(
                    null, null, null, null, null, payload, pageable);
            assertThat(byCarClass.getContent())
                    .as("findAllByFilter should return empty for carClass payload: %s", payload)
                    .isEmpty();
        }

        // Убедимся, что легитимные значения всё ещё работают
        Page<Contract> validStatus = contractRepository.findAllByFilter("ACTIVE", null, null, null, null, null, pageable);
        assertThat(validStatus.getContent()).contains(legitimateContract);

        Page<Contract> validBrand = contractRepository.findAllByFilter(null, null, null, "SAFE_BRAND", null, null, pageable);
        assertThat(validBrand.getContent()).contains(legitimateContract);

        Page<Contract> validBody = contractRepository.findAllByFilter(null, null, null, null, "SEDAN", null, pageable);
        assertThat(validBody.getContent()).contains(legitimateContract);

        Page<Contract> validClass = contractRepository.findAllByFilter(null, null, null, null, null, "LUXURY", pageable);
        assertThat(validClass.getContent()).contains(legitimateContract);
    }

}
