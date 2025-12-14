package org.example.carshering.repository.impl;

import org.example.carshering.fleet.infrastructure.persistence.entity.Model;
import org.example.carshering.repository.AbstractRepositoryTest;
import org.example.carshering.fleet.infrastructure.persistence.repository.ModelNameRepository;
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
public class ModelNameRepositoryTest extends AbstractRepositoryTest {

   @Autowired
    private ModelNameRepository modelNameRepository;

    @Autowired
    private DataUtils dataUtils;

    @BeforeEach
    public void setUp() {
        modelNameRepository.deleteAll();
    }
    @Test
    @DisplayName("Test save model functionality")
    public void givenModelObject_whenSave_thenModelIsCreated() {
        // given
        Model modelToSave = dataUtils.getModelNameTransient();

        // when
        Model savedModel = modelNameRepository.save(modelToSave);

        // then
        assertThat(savedModel).isNotNull();
        assertThat(savedModel.getId()).isNotNull();
        assertThat(savedModel.getName()).isEqualTo(modelToSave.getName());
    }

    @Test
    @DisplayName("Test update model functionality")
    public void givenModelToUpdate_whenSave_thenModelIsChanged() {
        // given
        Model modelToSave = dataUtils.getModelNameTransient();
        Model savedModel = modelNameRepository.save(modelToSave);

        String updatedName = "UpdatedModelName";

        // when
        savedModel.setName(updatedName);
        Model updatedModel = modelNameRepository.save(savedModel);

        // then
        assertThat(updatedModel).isNotNull();
        assertThat(updatedModel.getName()).isEqualTo(updatedName);
    }

    @Test
    @DisplayName("Test get model by id functionality")
    public void givenModelCreated_whenFindById_thenModelIsReturned() {
        // given
        Model modelToSave = dataUtils.getModelNameTransient();
        Model savedModel = modelNameRepository.save(modelToSave);

        // when
        Model obtainedModel = modelNameRepository.findById(savedModel.getId()).orElse(null);

        // then
        assertThat(obtainedModel).isNotNull();
        assertThat(obtainedModel.getName()).isEqualTo(modelToSave.getName());
    }

    @Test
    @DisplayName("Test model not found by id functionality")
    public void givenModelIsNotCreated_whenFindById_thenOptionalIsEmpty() {
        // when
        Model obtainedModel = modelNameRepository.findById(1L).orElse(null);

        // then
        assertThat(obtainedModel).isNull();
    }

    @Test
    @DisplayName("Test find model by name returns model when exists")
    public void givenModelExists_whenFindByNameIgnoreCase_thenModelIsReturned() {
        // given
        Model modelToSave = dataUtils.getModelNameTransient();
        modelNameRepository.save(modelToSave);

        // when
        Optional<Model> foundModel = modelNameRepository.findByNameIgnoreCase(modelToSave.getName());

        // then
        assertThat(foundModel).isPresent();
        assertThat(foundModel.get().getName()).isEqualTo(modelToSave.getName());
    }

    @Test
    @DisplayName("Test find model by name returns empty when model does not exist")
    public void givenModelDoesNotExist_whenFindByNameIgnoreCase_thenOptionalIsEmpty() {
        // when
        Optional<Model> foundModel = modelNameRepository.findByNameIgnoreCase("NonExistentModel");

        // then
        assertThat(foundModel).isEmpty();
    }

    @Test
    @DisplayName("Test find model by name is case-insensitive")
    public void givenModelExistsWithDifferentCase_whenFindByNameIgnoreCase_thenModelIsReturned() {
        // given
        Model model = Model.builder().name("CAMRY").build();
        modelNameRepository.save(model);

        // when
        Optional<Model> found1 = modelNameRepository.findByNameIgnoreCase("camry");
        Optional<Model> found2 = modelNameRepository.findByNameIgnoreCase("Camry");
        Optional<Model> found3 = modelNameRepository.findByNameIgnoreCase("CAMRY");

        // then
        assertThat(found1).isPresent().map(Model::getName).hasValue("CAMRY");
        assertThat(found2).isPresent().map(Model::getName).hasValue("CAMRY");
        assertThat(found3).isPresent().map(Model::getName).hasValue("CAMRY");
    }

    @Test
    @DisplayName("Test cannot save model with same name in different case due to unique constraint")
    void givenModelExists_whenSaveSameNameDifferentCase_thenThrowsDataIntegrityViolation() {
        // given
        modelNameRepository.save(Model.builder().name("CAMRY").build());

        // when & then
        Model duplicate = Model.builder().name("camry").build();
        assertThatThrownBy(() -> modelNameRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Test get all models functionality")
    public void givenThreeModelsAreStored_whenFindAll_thenAllModelsAreReturned() {
        // given
        Model model1 = dataUtils.getModelNameTransient();
        Model model2 = Model.builder().name("AnotherModel").build();
        Model model3 = Model.builder().name("ThirdModel").build();

        modelNameRepository.saveAll(List.of(model1, model2, model3));

        // when
        List<Model> obtainedModels = modelNameRepository.findAll();

        // then
        assertThat(CollectionUtils.isEmpty(obtainedModels)).isFalse();
        assertThat(obtainedModels).hasSize(3);
        assertThat(obtainedModels)
                .extracting(Model::getName)
                .containsExactlyInAnyOrder(model1.getName(), model2.getName(), model3.getName());
    }

    @Test
    @DisplayName("Test get all models when no models stored functionality")
    public void givenNoModelsAreStored_whenFindAll_thenEmptyListIsReturned() {
        // when
        List<Model> obtainedModels = modelNameRepository.findAll();

        // then
        assertThat(CollectionUtils.isEmpty(obtainedModels)).isTrue();
    }

    @Test
    @DisplayName("Test delete model by id functionality")
    public void givenModelIsSaved_whenDeleteById_thenModelIsRemoved() {
        // given
        Model modelToSave = dataUtils.getModelNameTransient();
        Model savedModel = modelNameRepository.save(modelToSave);

        // when
        modelNameRepository.deleteById(savedModel.getId());

        // then
        Model obtainedModel = modelNameRepository.findById(savedModel.getId()).orElse(null);
        assertThat(obtainedModel).isNull();
    }

    @Test
    @DisplayName("Test save model with duplicate name throws exception due to unique constraint")
    public void givenModelWithDuplicateName_whenSaved_thenThrowsDataIntegrityViolationException() {
        // given
        String modelName = "UniqueModel";
        Model model1 = Model.builder().name(modelName).build();
        modelNameRepository.save(model1);

        Model model2 = Model.builder().name(modelName).build();

        // when & then
        assertThatThrownBy(() -> modelNameRepository.saveAndFlush(model2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("unique");
    }


    /**
     * Проверяет, что метод {@link ModelNameRepository#findByNameIgnoreCase(String)} устойчив
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
        Model legitimateBrand = new Model();
        legitimateBrand.setName("MODEL");
        modelNameRepository.save(legitimateBrand);

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
            Optional<Model> result = modelNameRepository.findByNameIgnoreCase(input);

            // then
            assertThat(result).isEmpty(); // Никакой бренд с таким именем не существует
        }

        // Убедимся, что легитимный бренд всё ещё находится
        Optional<Model> found = modelNameRepository.findByNameIgnoreCase("model");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("MODEL");
    }

}