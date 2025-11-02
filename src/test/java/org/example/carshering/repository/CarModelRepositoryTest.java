package org.example.carshering.repository;

import org.example.carshering.entity.*;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(DataUtils.class)
@ActiveProfiles("test")
public class CarModelRepositoryTest extends AbstractRepositoryTest {



    private final Pageable pageable = PageRequest.of(0, 10);

    @Autowired
    private CarModelRepository carModelRepository;

    @Autowired
    private DataUtils dataUtils;


    @BeforeEach
    void setUp() {
        carModelRepository.deleteAll();
    }

    @Test
    @DisplayName("Test save car model functionality")
    public void givenCarModelObject_whenSave_thenCarModelIsCreated() {
        // given
        Car car = dataUtils.getCarWithSpecificAttributes("V1", "G1", 2020, "BRAND1", "MODEL1", "SEDAN", "CLASS1", "STATE1");
        CarModel modelToSave = car.getModel();

        // when
        CarModel saved = carModelRepository.save(modelToSave);

        // then
        assertThat(modelToSave).isNotNull();
        assertThat(saved).isNotNull();
        assertThat(saved.getIdModel()).isNotNull();
    }

    @Test
    @DisplayName("Test update car model functionality")
    public void givenCarModelToUpdate_whenSave_thenCarModelIsChanged() {
        // given
        Car car = dataUtils.getCarWithSpecificAttributes("V2", "G2", 2020, "BRAND2", "MODEL2", "SUV", "CLASS2", "STATE2");
        CarModel model = car.getModel();
        carModelRepository.save(model);

        // when
        CarModel modelToUpdate = carModelRepository.findById(model.getIdModel()).orElse(null);
        assertThat(modelToUpdate).isNotNull();
        modelToUpdate.setBodyType("HATCHBACK");
        CarModel updated = carModelRepository.save(modelToUpdate);

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getBodyType()).isEqualTo("HATCHBACK");
    }

    @Test
    @DisplayName("Test get car model by id functionality")
    public void givenCarModelCreated_whenFindById_thenCarModelIsReturned() {
        // given
        Car car = dataUtils.getCarWithSpecificAttributes("V3", "G3", 2020, "BRAND3", "MODEL3", "SEDAN", "CLASS3", "STATE3");
        CarModel model = carModelRepository.save(car.getModel());

        // when
        CarModel obtained = carModelRepository.findById(model.getIdModel()).orElse(null);

        // then
        assertThat(obtained).isNotNull();
        assertThat(obtained.getBodyType()).isEqualTo("SEDAN");
    }

    @Test
    @DisplayName("Test car model not found functionality")
    public void givenCarModelIsNotCreated_whenGetById_thenOptionalIsEmpty() {
        // when
        CarModel obtained = carModelRepository.findById(999L).orElse(null);

        // then
        assertThat(obtained).isNull();
    }

    @Test
    @DisplayName("findByIdAndDeletedFalse returns model when not deleted and empty when deleted")
    public void findByIdAndDeletedFalse_behaviour() {
        // given
        Car car = dataUtils.getCarWithSpecificAttributes("V4", "G4", 2020, "BRAND4", "MODEL4", "SEDAN", "CLASS4", "STATE4");
        CarModel model = car.getModel();
        carModelRepository.save(model);

        // when
        var found = carModelRepository.findByIdAndDeletedFalse(model.getIdModel()).orElse(null);

        // then
        assertThat(found).isNotNull();

        // when set deleted and save
        model.setDeleted(true);
        carModelRepository.save(model);

        var notFound = carModelRepository.findByIdAndDeletedFalse(model.getIdModel()).orElse(null);
        assertThat(notFound).isNull();
    }

    @Test
    @DisplayName("findModelsByFilter returns all models when all params null and includeDeleted true/false respects deleted flag")
    public void findModelsByFilter_basic() {
        // given
        var m1 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("VM1", "GM1", 2020, "BMW", "X5", "SUV", "LUXURY", "ACTIVE").getModel());
        var m2 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("VM2", "GM2", 2020, "BMW", "X3", "SUV", "LUXURY", "ACTIVE").getModel());
        var m3 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("VM3", "GM3", 2020, "AUDI", "A4", "SEDAN", "STANDARD", "ACTIVE").getModel());

        // when
        Page<CarModel> all = carModelRepository.findModelsByFilter(false, null, null, null, pageable);

        // then
        assertThat(all).isNotNull();
        assertThat(all.getContent()).hasSize(3);

        // test includeDeleted
        m1.setDeleted(true);
        carModelRepository.save(m1);

        Page<CarModel> withoutDeleted = carModelRepository.findModelsByFilter(false, null, null, null, pageable);
        Page<CarModel> withDeleted = carModelRepository.findModelsByFilter(true, null, null, null, pageable);

        assertThat(withoutDeleted.getContent()).hasSize(2);
        assertThat(withDeleted.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("findModelsByFilter filters by brand, bodyType and carClass")
    public void findModelsByFilter_filters() {
        // given
        var bmw1 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("VB1", "GB1", 2020, "BMW", "X5", "SUV", "LUXURY", "ACTIVE").getModel());
        var bmw2 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("VB2", "GB2", 2020, "BMW", "X3", "SUV", "LUXURY", "ACTIVE").getModel());
        var audi = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("VA1", "GA1", 2020, "AUDI", "A4", "SEDAN", "STANDARD", "ACTIVE").getModel());

        // when
        Page<CarModel> bmwResult = carModelRepository.findModelsByFilter(false, "BMW", null, null, pageable);
        Page<CarModel> sedanResult = carModelRepository.findModelsByFilter(false, null, "SEDAN", null, pageable);
        Page<CarModel> classResult = carModelRepository.findModelsByFilter(false, null, null, "LUXURY", pageable);

        // then
        assertThat(bmwResult.getContent()).hasSize(2);
        assertThat(bmwResult.getContent())
                .extracting(CarModel::getIdModel)
                .containsExactlyInAnyOrder(bmw1.getIdModel(), bmw2.getIdModel());

        assertThat(sedanResult.getContent()).hasSize(1);
        assertThat(sedanResult.getContent().get(0).getIdModel()).isEqualTo(audi.getIdModel());

        assertThat(classResult.getContent()).hasSize(2);
        assertThat(classResult.getContent())
                .extracting(cm -> cm.getCarClass().getName())
                .allMatch(name -> name.equals("LUXURY"));
    }

    @Test
    @DisplayName("findDistinctBodyTypes returns unique non-empty body types")
    public void findDistinctBodyTypes_basic() {
        // given
        var m1 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("VD1", "GD1", 2020, "BR1", "M1", "SEDAN", "C1", "S1").getModel());
        var m2 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("VD2", "GD2", 2020, "BR2", "M2", "SUV", "C2", "S1").getModel());
        var m3 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("VD3", "GD3", 2020, "BR3", "M3", "", "C3", "S1").getModel());

        // when
        List<String> bodyTypes = carModelRepository.findDistinctBodyTypes();

        // then
        assertThat(bodyTypes).contains("SEDAN", "SUV");
        assertThat(bodyTypes).doesNotContain("");
    }

    // --- New boundary and edge-case tests ---

    @Test
    @DisplayName("findByIdAndDeletedFalse returns empty when id does not exist")
    public void givenNonExistingId_whenFindByIdAndDeletedFalse_thenEmpty() {
        // when
        var found = carModelRepository.findByIdAndDeletedFalse(123456L).orElse(null);

        // then
        assertThat(found).isNull();
    }

    @Test
    @DisplayName("findModelsByFilter is case-insensitive and supports partial matches for brand")
    public void givenPartialBrand_whenFindModelsByFilter_thenMatchesReturnedCaseInsensitive() {
        // given
        var m1 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("P1", "G1", 2020, "BMW", "X5", "SUV", "LUXURY", "ACTIVE").getModel());
        var m2 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("P2", "G2", 2020, "bmw-inc", "X3", "SUV", "LUXURY", "ACTIVE").getModel());
        var m3 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("P3", "G3", 2020, "AUDI", "A4", "SEDAN", "STANDARD", "ACTIVE").getModel());

        // when: partial lowercase "bm" should match both BMW and bmw-inc
        Page<CarModel> result = carModelRepository.findModelsByFilter(false, "bm", null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(CarModel::getIdModel)
                .containsExactlyInAnyOrder(m1.getIdModel(), m2.getIdModel());
        assertThat(result.getContent())
                .noneMatch(cm -> cm.getIdModel().equals(m3.getIdModel()));
    }

    @Test
    @DisplayName("findModelsByFilter with empty strings behaves like no-filter (empty string matches all)")
    public void givenEmptyStringFilters_whenFindModelsByFilter_thenAllReturned() {
        // given
        var a = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("E1", "G1", 2020, "BR_A", "M_A", "SEDAN", "C_A", "S1").getModel());
        var b = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("E2", "G2", 2020, "BR_B", "M_B", "SUV", "C_B", "S1").getModel());

        // when: empty strings for brand/bodyType/carClass
        Page<CarModel> result = carModelRepository.findModelsByFilter(false, "", "", "", pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(CarModel::getIdModel)
                .containsExactlyInAnyOrder(a.getIdModel(), b.getIdModel());
    }

    @Test
    @DisplayName("findModelsByFilter returns empty page when pageable exceeds total results")
    public void givenPageExceedsResults_whenFindModelsByFilter_thenEmptyPage() {
        // given
        carModelRepository.save(dataUtils.getCarWithSpecificAttributes("PX1", "GX1", 2020, "BRX", "MX", "SEDAN", "C1", "S1").getModel());

        Pageable pageRequest = PageRequest.of(5, 10);

        // when
        Page<CarModel> result = carModelRepository.findModelsByFilter(false, null, null, null, pageRequest);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findDistinctBodyTypes excludes nulls and empty strings and deduplicates values")
    public void findDistinctBodyTypes_excludesNullsAndDuplicates() {
        // given
        var m1 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("D1", "G1", 2020, "B1", "M1", "SEDAN", "C1", "S1").getModel());
        var m2 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("D2", "G2", 2020, "B2", "M2", "SEDAN", "C2", "S1").getModel());
        var m3 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("D3", "G3", 2020, "B3", "M3", null, "C3", "S1").getModel());
        var m4 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("D4", "G4", 2020, "B4", "M4", "", "C4", "S1").getModel());

        // when
        List<String> bodyTypes = carModelRepository.findDistinctBodyTypes();

        // then
        assertThat(bodyTypes).containsExactlyInAnyOrder("SEDAN");
        assertThat(bodyTypes).doesNotContain("");
        assertThat(bodyTypes).doesNotContainNull();
    }

    @Test
    @DisplayName("findDistinctBodyTypes returns empty list when no models present")
    public void givenNoModels_whenFindDistinctBodyTypes_thenEmptyList() {
        // repository already cleaned in @BeforeEach
        List<String> bodyTypes = carModelRepository.findDistinctBodyTypes();
        assertThat(bodyTypes).isEmpty();
    }

    // --- Extra exhaustive scenarios for findModelsByFilter ---

    @Test
    @DisplayName("findModelsByFilter with '%' wildcard parameter returns all models")
    public void givenPercentWildcard_whenFindModelsByFilter_thenAllReturned() {
        // given
        var a = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("W1", "G1", 2020, "ZBR1", "M1", "SEDAN", "C1", "S1").getModel());
        var b = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("W2", "G2", 2020, "ZBR2", "M2", "SUV", "C2", "S1").getModel());

        // when: brand = "%" should act as wildcard and match all brands
        Page<CarModel> result = carModelRepository.findModelsByFilter(false, "%", null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(CarModel::getIdModel)
                .containsExactlyInAnyOrder(a.getIdModel(), b.getIdModel());
    }

    @Test
    @DisplayName("findModelsByFilter does not trim whitespace from filter values")
    public void givenFilterWithSpaces_whenFindModelsByFilter_thenNoMatchUnlessExactSpaces() {
        // given
        var m = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("S1", "G1", 2020, "SPBR", "M1", "SEDAN", "C1", "S1").getModel());

        // when: search with surrounding spaces — query does not trim, so should not match
        Page<CarModel> result = carModelRepository.findModelsByFilter(false, " SPBR ", null, null, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findModelsByFilter supports partial/case-insensitive match for bodyType and carClass")
    public void givenPartialBodyAndClassLowercase_whenFindModelsByFilter_thenMatchesReturned() {
        // given
        var m1 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("B1", "G1", 2020, "BRX", "M1", "HatchBack", "LUXURY-GOLD", "S1").getModel());
        var m2 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("B2", "G2", 2020, "BRY", "M2", "hatch", "luxury-silver", "S1").getModel());

        // when: partial lowercase "hatch" should match both HtachBack and hatch
        Page<CarModel> byBody = carModelRepository.findModelsByFilter(false, null, "hatch", null, pageable);
        Page<CarModel> byClass = carModelRepository.findModelsByFilter(false, null, null, "luxury", pageable);

        // then
        assertThat(byBody.getContent()).hasSize(2);
        assertThat(byBody.getContent())
                .extracting(CarModel::getIdModel)
                .containsExactlyInAnyOrder(m1.getIdModel(), m2.getIdModel());

        assertThat(byClass.getContent()).hasSize(2);
        assertThat(byClass.getContent())
                .extracting(cm -> cm.getCarClass().getName())
                .allMatch(name -> name.toLowerCase().contains("luxury"));
    }

    @Test
    @DisplayName("findModelsByFilter with brand+carClass that don't match together returns empty")
    public void givenBrandAndClassNonMatching_whenFindModelsByFilter_thenEmpty() {
        // given
        var m1 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("C1", "G1", 2020, "BRUN", "M1", "SEDAN", "CLASS-A", "S1").getModel());
        var m2 = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("C2", "G2", 2020, "BRUN", "M2", "SUV", "CLASS-B", "S1").getModel());

        // when: filter brand BRUN and class CLASS-C (no model has CLASS-C)
        Page<CarModel> result = carModelRepository.findModelsByFilter(false, "BRUN", null, "CLASS-C", pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findModelsByFilter excludes deleted models when includeDeleted=false even if other filters match")
    public void givenDeletedModel_whenFindModelsByFilterWithFilters_thenExcludedUnlessIncludeDeletedTrue() {
        // given
        var ok = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("DEL1", "G1", 2020, "DELBR", "M1", "SEDAN", "C1", "S1").getModel());
        var del = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("DEL2", "G2", 2020, "DELBR", "M2", "SEDAN", "C1", "S1").getModel());
        del.setDeleted(true);
        carModelRepository.save(del);

        // when
        Page<CarModel> withoutDeleted = carModelRepository.findModelsByFilter(false, "DELBR", null, null, pageable);
        Page<CarModel> withDeleted = carModelRepository.findModelsByFilter(true, "DELBR", null, null, pageable);

        // then
        assertThat(withoutDeleted.getContent()).hasSize(1);
        assertThat(withoutDeleted.getContent().get(0).getIdModel()).isEqualTo(ok.getIdModel());

        assertThat(withDeleted.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("findModelsByFilter treats strings containing only spaces as real filter (not empty)")
    public void givenOnlySpacesFilter_whenFindModelsByFilter_thenMatchesAccordingly() {
        // given
        var m = carModelRepository.save(dataUtils.getCarWithSpecificAttributes("SP2", "G1", 2020, "BR-SP", "M1", "SEDAN", "C1", "S1").getModel());

        // when: pass single space as brand — since query only treats empty string as no-filter, this is used in LIKE
        Page<CarModel> result = carModelRepository.findModelsByFilter(false, " ", null, null, pageable);

        // then
        // very likely no brand contains a space, so expect empty
        assertThat(result.getContent()).isEmpty();
    }



    /**
     * Проверяет устойчивость метода {@link CarModelRepository#findModelsByFilter(boolean, String, String, String, Pageable)}
     * к SQL-инъекциям через строковые параметры: brand, bodyType, carClass.
     * <p>
     * Все входные данные передаются как {@code String} и могут быть подконтрольны атакующему.
     * Тест убеждается, что злонамеренные строки (включая классические паттерны инъекций)
     * интерпретируются как литералы, не приводят к выполнению произвольного SQL
     * и не возвращают неавторизованные данные.
     * <p>
     * Контекст безопасности:
     * <ul>
     *   <li>{@code findByIdAndDeletedFalse(Long id)} принимает {@code Long}, а не строку — SQL-инъекция невозможна.</li>
     *   <li>{@code findDistinctBodyTypes()} не имеет параметров — неуязвим по определению.</li>
     *   <li>Все остальные методы с пользовательским вводом строго контролируются: либо типизированы,
     *       либо используют параметризованные запросы / Criteria API.</li>
     * </ul>
     * <p>
     * Тест сохраняет легитимную модель, затем подаёт в каждый фильтр по отдельности
     * известные векторы атак. Ожидается: пустой результат и отсутствие исключений.
     * В конце проверяется корректность легитимного поиска.
     */
    @Test
    @DisplayName("findModelsByFilter is safe against SQL injection in brand, bodyType, carClass parameters")
    void findModelsByFilter_sqlInjectionAttempts_returnEmptyOrSafe() {
        // given

        CarModel validModel = dataUtils.createAndSaveCarModel();



        List<String> injectionAttempts = Arrays.asList(
                "' OR '1'='1",
                "'; DROP TABLE brands; --",
                "admin'--",
                "\" OR \"\"=\"",
                "'; SELECT * FROM information_schema.tables; --",
                "1' UNION SELECT null,null,null--",
                "'; EXEC xp_cmdshell('ping 127.0.0.1') --",
                "/*",
                "*/ UNION SELECT * FROM brands; /*",
                "\\'; DROP DATABASE; --"
        );

        // When & Then
        for (String attempt : injectionAttempts) {
            Page<CarModel> resultByBrand = carModelRepository.findModelsByFilter(
                    false, attempt, null, null, Pageable.unpaged());
            Page<CarModel> resultByBodyType = carModelRepository.findModelsByFilter(
                    false, null, attempt, null, Pageable.unpaged());
            Page<CarModel> resultByCarClass = carModelRepository.findModelsByFilter(
                    false, null, null, attempt, Pageable.unpaged());

            assertThat(resultByBrand.getContent()).doesNotContain(validModel);
            assertThat(resultByBodyType.getContent()).doesNotContain(validModel);
            assertThat(resultByCarClass.getContent()).doesNotContain(validModel);


            assertThat(resultByBrand).isEmpty();
            assertThat(resultByBodyType).isEmpty();
            assertThat(resultByCarClass).isEmpty();
        }
    }

}
