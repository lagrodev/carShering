package org.example.carshering.repository.impl;

import org.example.carshering.rental.infrastructure.persistence.entity.RentalState;
import org.example.carshering.repository.AbstractRepositoryTest;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
@Import(DataUtils.class)
@ActiveProfiles("test")
public class RentalStateRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private RentalStateRepository rentalStateRepository;

    @Autowired
    private DataUtils dataUtils;

    @BeforeEach
    void setUp() {
        rentalStateRepository.deleteAll();
    }

    @Test
    @DisplayName("Test save rental state functionality")
    public void givenRentalStateObject_whenSave_thenRentalStateIsCreated() {
        // given
        RentalState rentalStateToSave = dataUtils.getRentalStateTransient();

        // when
        RentalState savedRentalState = rentalStateRepository.save(rentalStateToSave);

        // then
        assertThat(savedRentalState).isNotNull();
        assertThat(savedRentalState.getId()).isNotNull();
        assertThat(savedRentalState.getName()).isEqualTo(rentalStateToSave.getName());
    }

    @Test
    @DisplayName("Test update rental state functionality")
    public void givenRentalStateToUpdate_whenSave_thenRentalStateIsChanged() {
        // given
        RentalState rentalStateToSave = dataUtils.getRentalStateTransient();
        RentalState savedRentalState = rentalStateRepository.save(rentalStateToSave);

        String updatedName = "UpdatedRentalState";

        // when
        savedRentalState.setName(updatedName);
        RentalState updatedRentalState = rentalStateRepository.save(savedRentalState);

        // then
        assertThat(updatedRentalState).isNotNull();
        assertThat(updatedRentalState.getName()).isEqualTo(updatedName);
    }

    @Test
    @DisplayName("Test get rental state by id functionality")
    public void givenRentalStateCreated_whenFindById_thenRentalStateIsReturned() {
        // given
        RentalState rentalStateToSave = dataUtils.getRentalStateTransient();
        RentalState savedRentalState = rentalStateRepository.save(rentalStateToSave);

        // when
        RentalState obtainedRentalState = rentalStateRepository.findById(savedRentalState.getId()).orElse(null);

        // then
        assertThat(obtainedRentalState).isNotNull();
        assertThat(obtainedRentalState.getName()).isEqualTo(rentalStateToSave.getName());
    }

    @Test
    @DisplayName("Test rental state not found by id functionality")
    public void givenRentalStateIsNotCreated_whenFindById_thenOptionalIsEmpty() {
        // when
        RentalState obtainedRentalState = rentalStateRepository.findById(1L).orElse(null);

        // then
        assertThat(obtainedRentalState).isNull();
    }

    @Test
    @DisplayName("Test find rental state by name returns rental state when exists")
    public void givenRentalStateExists_whenFindByNameIgnoreCase_thenRentalStateIsReturned() {
        // given
        RentalState rentalStateToSave = dataUtils.getRentalStateTransient();
        rentalStateRepository.save(rentalStateToSave);

        // when
        Optional<RentalState> foundRentalState = rentalStateRepository.findByNameIgnoreCase(rentalStateToSave.getName());

        // then
        assertThat(foundRentalState).isPresent();
        assertThat(foundRentalState.get().getName()).isEqualTo(rentalStateToSave.getName());
    }

    @Test
    @DisplayName("Test find rental state by name returns empty when rental state does not exist")
    public void givenRentalStateDoesNotExist_whenFindByNameIgnoreCase_thenOptionalIsEmpty() {
        // when
        Optional<RentalState> foundRentalState = rentalStateRepository.findByNameIgnoreCase("NonExistentRentalState");

        // then
        assertThat(foundRentalState).isEmpty();
    }

    @Test
    @DisplayName("Test find rental state by name is case-insensitive")
    public void givenRentalStateExistsWithDifferentCase_whenFindByNameIgnoreCase_thenRentalStateIsReturned() {
        // given
        RentalState rentalState = RentalState.builder().name("ACTIVE").build();
        rentalStateRepository.save(rentalState);

        // when
        Optional<RentalState> found1 = rentalStateRepository.findByNameIgnoreCase("active");
        Optional<RentalState> found2 = rentalStateRepository.findByNameIgnoreCase("Active");
        Optional<RentalState> found3 = rentalStateRepository.findByNameIgnoreCase("ACTIVE");

        // then
        assertThat(found1).isPresent().map(RentalState::getName).hasValue("ACTIVE");
        assertThat(found2).isPresent().map(RentalState::getName).hasValue("ACTIVE");
        assertThat(found3).isPresent().map(RentalState::getName).hasValue("ACTIVE");
    }

    @Test
    @DisplayName("Test cannot save rental state with same name in different case due to unique constraint")
    void givenRentalStateExists_whenSaveSameNameDifferentCase_thenThrowsDataIntegrityViolation() {
        // given
        rentalStateRepository.save(RentalState.builder().name("COMPLETED").build());

        // when & then
        RentalState duplicate = RentalState.builder().name("completed").build();
        assertThatThrownBy(() -> rentalStateRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Test get all rental states functionality")
    public void givenThreeRentalStatesAreStored_whenFindAll_thenAllRentalStatesAreReturned() {
        // given
        RentalState state1 = dataUtils.getRentalStateTransient();
        RentalState state2 = RentalState.builder().name("AnotherRentalState").build();
        RentalState state3 = RentalState.builder().name("ThirdRentalState").build();

        rentalStateRepository.saveAll(List.of(state1, state2, state3));

        // when
        List<RentalState> obtainedRentalStates = rentalStateRepository.findAll();

        // then
        assertThat(CollectionUtils.isEmpty(obtainedRentalStates)).isFalse();
        assertThat(obtainedRentalStates).hasSize(3);
        assertThat(obtainedRentalStates)
                .extracting(RentalState::getName)
                .containsExactlyInAnyOrder(state1.getName(), state2.getName(), state3.getName());
    }

    @Test
    @DisplayName("Test get all rental states when no rental states stored functionality")
    public void givenNoRentalStatesAreStored_whenFindAll_thenEmptyListIsReturned() {
        // when
        List<RentalState> obtainedRentalStates = rentalStateRepository.findAll();

        // then
        assertThat(CollectionUtils.isEmpty(obtainedRentalStates)).isTrue();
    }

    @Test
    @DisplayName("Test delete rental state by id functionality")
    public void givenRentalStateIsSaved_whenDeleteById_thenRentalStateIsRemoved() {
        // given
        RentalState rentalStateToSave = dataUtils.getRentalStateTransient();
        RentalState savedRentalState = rentalStateRepository.save(rentalStateToSave);

        // when
        rentalStateRepository.deleteById(savedRentalState.getId());

        // then
        RentalState obtainedRentalState = rentalStateRepository.findById(savedRentalState.getId()).orElse(null);
        assertThat(obtainedRentalState).isNull();
    }

    @Test
    @DisplayName("Test save rental state with duplicate name throws exception due to unique constraint")
    public void givenRentalStateWithDuplicateName_whenSaved_thenThrowsDataIntegrityViolationException() {
        // given
        String rentalStateName = "UniqueRentalState";
        RentalState state1 = RentalState.builder().name(rentalStateName).build();
        rentalStateRepository.save(state1);

        RentalState state2 = RentalState.builder().name(rentalStateName).build();

        // when & then
        assertThatThrownBy(() -> rentalStateRepository.saveAndFlush(state2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("unique");
    }

    /**
     * Проверяет, что метод {@link RentalStateRepository#findByNameIgnoreCase(String)} устойчив
     * к попыткам SQL-инъекции: злонамеренные входные строки интерпретируются как обычные
     * строковые значения, а не исполняемый SQL-код.
     * <p>
     * Тест сохраняет легитимный бренд ("MODEL"), затем пытается найти модуль с именами,
     * содержащими типичные паттерны SQL-инъекций. Ожидается, что результат всегда пустой.
     * После этого проверяется, что легитимный бренд по-прежнему находится при корректном запросе.
     * <p>
     * Цель: подтвердить защиту на уровне репозитория без полагания только на Spring Data JPA.
     */
    @Test
    @DisplayName("findByNameIgnoreCase is safe against SQL injection attempts")
    void findByNameIgnoreCase_sqlInjectionAttempt_returnsEmpty() {
        // given
        RentalState legitimateBrand = new RentalState();
        legitimateBrand.setName("MODEL");
        rentalStateRepository.save(legitimateBrand);

        // Популярные паттерны SQL-инъекций
        String[] maliciousInputs = {
                "' OR '1'='1' --",
                "'; DROP TABLE model; --",
                "' UNION SELECT null, version() --",
                "\" OR \"\"=\"",
                "admin'--",
                "'; SELECT * FROM users; --"
        };

        for (String input : maliciousInputs) {
            // when
            Optional<RentalState> result = rentalStateRepository.findByNameIgnoreCase(input);

            // then
            assertThat(result).isEmpty(); // Никакой бренд с таким именем не существует
        }

        // Убедимся, что легитимный бренд всё ещё находится
        Optional<RentalState> found = rentalStateRepository.findByNameIgnoreCase("model");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("MODEL");
    }


}