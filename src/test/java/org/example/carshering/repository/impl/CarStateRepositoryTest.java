package org.example.carshering.repository.impl;

import org.example.carshering.domain.entity.CarState;
import org.example.carshering.repository.AbstractRepositoryTest;
import org.example.carshering.repository.CarStateRepository;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
@Import(DataUtils.class)
@ActiveProfiles("test")
public class CarStateRepositoryTest extends AbstractRepositoryTest {


    @Autowired
    private CarStateRepository carStateRepository;

    @Autowired
    private DataUtils dataUtils;
    @BeforeEach
    public void setUp() {
        carStateRepository.deleteAll();
    }

    @Test
    @DisplayName("Test save car state functionality")
    public void givenCarStateObject_whenSave_thenCarStateIsCreated() {
        // given
        CarState carStateToSave = dataUtils.getCarStateTransient();

        // when
        CarState savedCarState = carStateRepository.save(carStateToSave);

        // then
        assertThat(savedCarState).isNotNull();
        assertThat(savedCarState.getId()).isNotNull();
        assertThat(savedCarState.getStatus()).isEqualTo(carStateToSave.getStatus());
    }

    @Test
    @DisplayName("Test update car state functionality")
    public void givenCarStateToUpdate_whenSave_thenCarStateIsChanged() {
        // given
        CarState carStateToSave = dataUtils.getCarStateTransient();
        CarState savedCarState = carStateRepository.save(carStateToSave);

        String updatedStatus = "Unavailable";

        // when
        savedCarState.setStatus(updatedStatus);
        CarState updatedCarState = carStateRepository.save(savedCarState);

        // then
        assertThat(updatedCarState).isNotNull();
        assertThat(updatedCarState.getStatus()).isEqualTo(updatedStatus);
    }

    @Test
    @DisplayName("Test get car state by id functionality")
    public void givenCarStateCreated_whenFindById_thenCarStateIsReturned() {
        // given
        CarState carStateToSave = dataUtils.getCarStateTransient();
        CarState savedCarState = carStateRepository.save(carStateToSave);

        // when
        CarState obtainedCarState = carStateRepository.findById(
                savedCarState.getId()).orElse(null);

        // then
        assertThat(obtainedCarState).isNotNull();
        assertThat(obtainedCarState.getStatus()).isEqualTo(carStateToSave.getStatus());
    }

    @Test
    @DisplayName("Test car state not found by id functionality")
    public void givenCarStateIsNotCreated_whenFindById_thenOptionalIsEmpty() {
        // when
        CarState obtainedCarState = carStateRepository.findById(1).orElse(null);

        // then
        assertThat(obtainedCarState).isNull();
    }

    @Test
    @DisplayName("Test find car state by status returns car state when exists")
    public void givenCarStateExists_whenFindByStatusIgnoreCase_thenCarStateIsReturned() {
        // given
        CarState carStateToSave = dataUtils.getCarStateTransient();
        carStateRepository.save(carStateToSave);

        // when
        Optional<CarState> foundCarState = carStateRepository.findByStatusIgnoreCase(carStateToSave.getStatus());

        // then
        assertThat(foundCarState).isPresent();
        assertThat(foundCarState.get().getStatus()).isEqualTo(carStateToSave.getStatus());
    }

    @Test
    @DisplayName("Test find car state by status returns empty when car state does not exist")
    public void givenCarStateDoesNotExist_whenFindByStatusIgnoreCase_thenOptionalIsEmpty() {
        // when
        Optional<CarState> foundCarState = carStateRepository.findByStatusIgnoreCase("NonExistentStatus");

        // then
        assertThat(foundCarState).isEmpty();
    }

    @Test
    @DisplayName("Test find car state by status is case-insensitive")
    public void givenCarStateExistsWithDifferentCase_whenFindByStatusIgnoreCase_thenCarStateIsReturned() {
        // given
        CarState carState = CarState.builder().status("AVAILABLE").build();
        carStateRepository.save(carState);

        // when
        Optional<CarState> found1 = carStateRepository.findByStatusIgnoreCase("available");
        Optional<CarState> found2 = carStateRepository.findByStatusIgnoreCase("Available");
        Optional<CarState> found3 = carStateRepository.findByStatusIgnoreCase("AVAILABLE");

        // then
        assertThat(found1).isPresent().map(CarState::getStatus).hasValue("AVAILABLE");
        assertThat(found2).isPresent().map(CarState::getStatus).hasValue("AVAILABLE");
        assertThat(found3).isPresent().map(CarState::getStatus).hasValue("AVAILABLE");
    }

    @Test
    @DisplayName("Test cannot save car state with same status in different case due to unique constraint")
    void givenCarStateExists_whenSaveSameStatusDifferentCase_thenThrowsDataIntegrityViolation() {
        // given
        carStateRepository.save(CarState.builder().status("AVAILABLE").build());

        // when & then
        CarState duplicate = CarState.builder().status("available").build();
        assertThatThrownBy(() -> carStateRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Test get all car states functionality")
    public void givenThreeCarStatesAreStored_whenFindAll_thenAllCarStatesAreReturned() {
        // given
        CarState state1 = dataUtils.getCarStateTransient();
        CarState state2 = CarState.builder().status("Reserved").build();
        CarState state3 = CarState.builder().status("Maintenance").build();

        carStateRepository.saveAll(List.of(state1, state2, state3));

        // when
        List<CarState> obtainedCarStates = carStateRepository.findAll();

        // then
        assertThat(CollectionUtils.isEmpty(obtainedCarStates)).isFalse();
        assertThat(obtainedCarStates).hasSize(3);
        assertThat(obtainedCarStates)
                .extracting(CarState::getStatus)
                .containsExactlyInAnyOrder(state1.getStatus(), state2.getStatus(), state3.getStatus());
    }

    @Test
    @DisplayName("Test get all car states when no car states stored functionality")
    public void givenNoCarStatesAreStored_whenFindAll_thenEmptyListIsReturned() {
        // when
        List<CarState> obtainedCarStates = carStateRepository.findAll();

        // then
        assertThat(CollectionUtils.isEmpty(obtainedCarStates)).isTrue();
    }

    @Test
    @DisplayName("Test delete car state by id functionality")
    public void givenCarStateIsSaved_whenDeleteById_thenCarStateIsRemoved() {
        // given
        CarState carStateToSave = dataUtils.getCarStateTransient();
        CarState savedCarState = carStateRepository.save(carStateToSave);

        // when
        carStateRepository.deleteById(
                savedCarState.getId());

        // then
        CarState obtainedCarState = carStateRepository.findById(savedCarState.getId()).orElse(null);
        assertThat(obtainedCarState).isNull();
    }

    @Test
    @DisplayName("Test save car state with duplicate status throws exception due to unique constraint")
    public void givenCarStateWithDuplicateStatus_whenSaved_thenThrowsDataIntegrityViolationException() {
        // given
        String status = "Active";
        CarState state1 = CarState.builder().status(status).build();
        carStateRepository.save(state1);

        CarState state2 = CarState.builder().status(status).build();

        // when & then
        assertThatThrownBy(() -> carStateRepository.saveAndFlush(state2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("unique");
    }


    /**
     * Проверяет устойчивость метода {@link CarStateRepository#findByStatusIgnoreCase(String)}
     * к SQL-инъекциям через строковый параметр {@code status}.
     * <p>
     * Поскольку входной параметр — произвольная строка от клиента, он потенциально уязвим,
     * если реализация использует конкатенацию SQL. Тест подаёт известные векторы атак
     * и убеждается, что:
     * <ul>
     *   <li>Результат всегда {@code Optional.empty()} для злонамеренных значений;</li>
     *   <li>Не происходит выполнения внестрокового SQL;</li>
     *   <li>Легитимный статус ("ACTIVE") по-прежнему находится в любом регистре.</li>
     * </ul>
     * <p>
     * Почему НЕ тестируются {@code findById(Long)} и {@code deleteById(Long)}:
     * <ul>
     *   <li>Оба метода принимают параметр типа {@code Long} — не строку.</li>
     *   <li>Spring Data JPA преобразует {@code Long} в SQL-параметр напрямую (например, {@code WHERE id = ?}),
     *       без интерпретации содержимого как SQL-кода.</li>
     *   <li>Даже при передаче из HTTP (где всё приходит как строка), Spring сначала пытается
     *       распарсить значение в {@code Long}. При неудаче — выбрасывает {@code TypeMismatchException}
     *       ДО попадания в репозиторий.</li>
     *   <li>Следовательно, SQL-инъекция через {@code id} технически невозможна — нет пути
     *       для внедрения синтаксиса SQL в типизированный числовой параметр.</li>
     * </ul>
     * <p>
     * Вывод: только методы с {@code String}-параметрами, зависящими от пользователя,
     * требуют явной проверки на SQL-инъекции.
     */

    @Test
    @DisplayName("findByStatusIgnoreCase is safe against SQL injection attempts")
    void findByStatusIgnoreCase_sqlInjectionAttempts_returnEmptyAndDoNotExposeData() {
        // given
        CarState legitimateState = CarState.builder().status("ACTIVE").build();
        carStateRepository.save(legitimateState);

        List<String> injectionAttempts = Arrays.asList(
                "' OR '1'='1",
                "'; DROP TABLE car_states; --",
                "ACTIVE'--",
                "ACTIVE' OR 'x'='x",
                "\" OR \"\"=\"",
                "*/ UNION SELECT * FROM car_states; /*",
                "\\'; DELETE FROM car_states; --",
                "x' AND 1=1; --",
                "/*",
                "1' UNION SELECT null--"
        );

        // when & then
        for (String payload : injectionAttempts) {
            Optional<CarState> result = carStateRepository.findByStatusIgnoreCase(payload);

            // Должен вернуть empty, потому что ни один статус не равен в точности payload
            assertThat(result)
                    .as("findByStatusIgnoreCase should return empty for payload: %s", payload)
                    .isEmpty();
        }

        // Убедимся, что легитимный статус всё ещё находится (в разных регистрах)
        assertThat(carStateRepository.findByStatusIgnoreCase("active")).isPresent();
        assertThat(carStateRepository.findByStatusIgnoreCase("Active")).isPresent();
        assertThat(carStateRepository.findByStatusIgnoreCase("ACTIVE")).isPresent();
    }

}