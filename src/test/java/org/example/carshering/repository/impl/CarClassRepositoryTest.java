package org.example.carshering.repository.impl;

import org.example.carshering.domain.entity.CarClass;
import org.example.carshering.repository.AbstractRepositoryTest;
import org.example.carshering.repository.CarClassRepository;
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
public class CarClassRepositoryTest extends AbstractRepositoryTest {


    @BeforeEach
    public void setUp() {
        carClassRepository.deleteAll();
    }

    // <!-- Tests --> //
    @Autowired
    private CarClassRepository carClassRepository;
    @Autowired
    private DataUtils dataUtils;

    @Test
    @DisplayName("Test save car class functionality")
    public void givenCarClassObject_whenSave_thenCarClassIsCreated() {
        // given
        CarClass carClassToSave = dataUtils.getCarClassTransient();

        // when
        CarClass savedCarClass = carClassRepository.save(carClassToSave);

        // then
        assertThat(savedCarClass).isNotNull();
        assertThat(savedCarClass.getId()).isNotNull();
        assertThat(savedCarClass.getName()).isEqualTo(carClassToSave.getName());
    }

    @Test
    @DisplayName("Test update car class functionality")
    public void givenCarClassToUpdate_whenSave_thenCarClassIsChanged() {
        // given
        CarClass carClassToSave = dataUtils.getCarClassTransient();
        CarClass savedCarClass = carClassRepository.save(carClassToSave);

        String updatedName = "UpdatedCarClass";

        // when
        savedCarClass.setName(updatedName);
        CarClass updatedCarClass = carClassRepository.save(savedCarClass);

        // then
        assertThat(updatedCarClass).isNotNull();
        assertThat(updatedCarClass.getName()).isEqualTo(updatedName);
    }

    @Test
    @DisplayName("Test get car class by id functionality")
    public void givenCarClassCreated_whenFindById_thenCarClassIsReturned() {
        // given
        CarClass carClassToSave = dataUtils.getCarClassTransient();
        CarClass savedCarClass = carClassRepository.save(carClassToSave);

        // when
        CarClass obtainedCarClass = carClassRepository.findById(savedCarClass.getId()).orElse(null);

        // then
        assertThat(obtainedCarClass).isNotNull();
        assertThat(obtainedCarClass.getName()).isEqualTo(carClassToSave.getName());
    }

    @Test
    @DisplayName("Test car class not found by id functionality")
    public void givenCarClassIsNotCreated_whenFindById_thenOptionalIsEmpty() {
        // when
        CarClass obtainedCarClass = carClassRepository.findById(1L).orElse(null);

        // then
        assertThat(obtainedCarClass).isNull();
    }

    @Test
    @DisplayName("Test find car class by name returns car class when exists")
    public void givenCarClassExists_whenFindByNameIgnoreCase_thenCarClassIsReturned() {
        // given
        CarClass carClassToSave = dataUtils.getCarClassTransient();
        carClassRepository.save(carClassToSave);

        // when
        Optional<CarClass> foundCarClass = carClassRepository.findByNameIgnoreCase(carClassToSave.getName());

        // then
        assertThat(foundCarClass).isPresent();
        assertThat(foundCarClass.get().getName()).isEqualTo(carClassToSave.getName());
    }

    @Test
    @DisplayName("Test find car class by name returns empty when car class does not exist")
    public void givenCarClassDoesNotExist_whenFindByNameIgnoreCase_thenOptionalIsEmpty() {
        // when
        Optional<CarClass> foundCarClass = carClassRepository.findByNameIgnoreCase("NonExistentCarClass");

        // then
        assertThat(foundCarClass).isEmpty();
    }

    @Test
    @DisplayName("Test find car class by name is case-insensitive")
    public void givenCarClassExistsWithDifferentCase_whenFindByNameIgnoreCase_thenCarClassIsReturned() {
        // given
        CarClass carClass = CarClass.builder().name("ECONOMY").build();
        carClassRepository.save(carClass);

        // when
        Optional<CarClass> found1 = carClassRepository.findByNameIgnoreCase("economy");
        Optional<CarClass> found2 = carClassRepository.findByNameIgnoreCase("Economy");
        Optional<CarClass> found3 = carClassRepository.findByNameIgnoreCase("ECONOMY");

        // then
        assertThat(found1).isPresent().map(CarClass::getName).hasValue("ECONOMY");
        assertThat(found2).isPresent().map(CarClass::getName).hasValue("ECONOMY");
        assertThat(found3).isPresent().map(CarClass::getName).hasValue("ECONOMY");
    }

    @Test
    @DisplayName("Test cannot save car class with same name in different case due to unique constraint")
    void givenCarClassExists_whenSaveSameNameDifferentCase_thenThrowsDataIntegrityViolation() {
        // given
        carClassRepository.save(CarClass.builder().name("ECONOMY").build());

        // when & then
        CarClass duplicate = CarClass.builder().name("economy").build();
        assertThatThrownBy(() -> carClassRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Test get all car classes functionality")
    public void givenThreeCarClassesAreStored_whenFindAll_thenAllCarClassesAreReturned() {
        // given
        CarClass class1 = dataUtils.getCarClassTransient();
        CarClass class2 = CarClass.builder().name("Premium").build();
        CarClass class3 = CarClass.builder().name("SUV").build();

        carClassRepository.saveAll(List.of(class1, class2, class3));

        // when
        List<CarClass> obtainedCarClasses = carClassRepository.findAll();

        // then
        assertThat(CollectionUtils.isEmpty(obtainedCarClasses)).isFalse();
        assertThat(obtainedCarClasses).hasSize(3);
        assertThat(obtainedCarClasses)
                .extracting(CarClass::getName)
                .containsExactlyInAnyOrder(class1.getName(), class2.getName(), class3.getName());
    }

    @Test
    @DisplayName("Test get all car classes when no car classes stored functionality")
    public void givenNoCarClassesAreStored_whenFindAll_thenEmptyListIsReturned() {
        // when
        List<CarClass> obtainedCarClasses = carClassRepository.findAll();

        // then
        assertThat(CollectionUtils.isEmpty(obtainedCarClasses)).isTrue();
    }

    @Test
    @DisplayName("Test delete car class by id functionality")
    public void givenCarClassIsSaved_whenDeleteById_thenCarClassIsRemoved() {
        // given
        CarClass carClassToSave = dataUtils.getCarClassTransient();
        CarClass savedCarClass = carClassRepository.save(carClassToSave);

        // when
        carClassRepository.deleteById(savedCarClass.getId());

        // then
        CarClass obtainedCarClass = carClassRepository.findById(savedCarClass.getId()).orElse(null);
        assertThat(obtainedCarClass).isNull();
    }

    @Test
    @DisplayName("Test save car class with duplicate name throws exception due to unique constraint")
    public void givenCarClassWithDuplicateName_whenSaved_thenThrowsDataIntegrityViolationException() {
        // given
        String className = "UniqueClass";
        CarClass class1 = CarClass.builder().name(className).build();
        carClassRepository.save(class1);

        CarClass class2 = CarClass.builder().name(className).build();

        // when & then
        assertThatThrownBy(() -> carClassRepository.saveAndFlush(class2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("unique");
    }


    /**
     * Проверяет, что метод {@link CarClassRepository#findByNameIgnoreCase(String)} устойчив
     * к попыткам SQL-инъекции: злонамеренные входные строки интерпретируются как обычные
     * строковые значения, а не исполняемый SQL-код.
     * <p>
     * Тест сохраняет легитимный бренд ("ILoveFuckingTests"), затем пытается найти бренды с именами,
     * содержащими типичные паттерны SQL-инъекций. Ожидается, что результат всегда пустой.
     * После этого проверяется, что легитимный бренд по-прежнему находится при корректном запросе.
     * <p>
     * Цель: подтвердить защиту на уровне репозитория без полагания только на Spring Data JPA.
     */
    @Test
    @DisplayName("findByNameIgnoreCase is safe against SQL injection attempts")
    void findByNameIgnoreCase_sqlInjectionAttempt_returnsEmpty() {
        // given
        CarClass legitimateCarClass = new CarClass();
        legitimateCarClass.setName("ILoveFuckingTests");
        carClassRepository.save(legitimateCarClass);

        // Популярные паттерны SQL-инъекций
        String[] maliciousInputs = {
                "' OR '1'='1' --",
                "'; DROP TABLE brands; --",
                "' UNION SELECT null, version() --",
                "\" OR \"\"=\"",
                "admin'--",
                "'; SELECT * FROM users; --"
        };

        for (String input : maliciousInputs) {
            // when
            Optional<CarClass> result = carClassRepository.findByNameIgnoreCase(input);

            // then
            assertThat(result).isEmpty(); // Никакой бренд с таким именем не существует
        }

        // Убедимся, что легитимный класс всё ещё находится
        Optional<CarClass> found = carClassRepository.findByNameIgnoreCase("ilovefuckingtests");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ILoveFuckingTests");
    }

}