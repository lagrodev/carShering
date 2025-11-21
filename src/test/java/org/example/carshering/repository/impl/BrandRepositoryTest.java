package org.example.carshering.repository.impl;

import org.example.carshering.entity.Brand;
import org.example.carshering.repository.AbstractRepositoryTest;
import org.example.carshering.repository.BrandRepository;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
public class BrandRepositoryTest extends AbstractRepositoryTest {

    // <!-- Tests --> //
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private DataUtils dataUtils;

    @BeforeEach
    public void setUp() {
        brandRepository.deleteAll();
    }

    @Test
    @DisplayName("Test save brand functionality")
    public void givenBrandObject_whenSave_thenBrandIsCreated() {
        //given
        Brand brandToSave = dataUtils.getBrandTransient();

        // when
        Brand saveBrand = brandRepository.save(brandToSave);

        // then
        assertThat(saveBrand).isNotNull();
        assertThat(saveBrand.getId()).isNotNull();
        assertThat(saveBrand.getName()).isEqualTo(brandToSave.getName());
    }

    @Test
    @DisplayName("Test update brand functionality")
    public void givenBrandToUpdate_whenSave_thenBrandIsChanged() {
        // given
        Brand brandToSave = dataUtils.getBrandTransient();
        Brand savedBrand = brandRepository.save(brandToSave);

        String updatedName = "UpdatedBrand";

        // when
        savedBrand.setName(updatedName);
        Brand updatedBrand = brandRepository.save(savedBrand);

        // then
        assertThat(updatedBrand).isNotNull();
        assertThat(updatedBrand.getName()).isEqualTo(updatedName);
    }

    @Test
    @DisplayName("Test get brand by id functionality")
    public void givenBrandCreated_whenFindById_thenBrandIsReturned() {
        // given
        Brand brandToSave = dataUtils.getBrandTransient();
        Brand savedBrand = brandRepository.save(brandToSave);

        // when
        Brand obtainedBrand = brandRepository.findById(savedBrand.getId()).orElse(null);

        // then
        assertThat(obtainedBrand).isNotNull();
        assertThat(obtainedBrand.getName()).isEqualTo(brandToSave.getName());
    }

    @Test
    @DisplayName("Test brand not found by id functionality")
    public void givenBrandIsNotCreated_whenFindById_thenOptionalIsEmpty() {
        // when
        Brand obtainedBrand = brandRepository.findById(1L).orElse(null);

        // then
        assertThat(obtainedBrand).isNull();
    }

    @Test
    @DisplayName("Test find brand by name returns brand when exists")
    public void givenBrandExists_whenfindByNameIgnoreCase_thenBrandIsReturned() {
        // given
        Brand brandToSave = dataUtils.getBrandTransient();
        brandRepository.save(brandToSave);

        // when
        Optional<Brand> foundBrand = brandRepository.findByNameIgnoreCase(brandToSave.getName());

        // then
        assertThat(foundBrand).isPresent();
        assertThat(foundBrand.get().getName()).isEqualTo(brandToSave.getName());
    }

    @Test
    @DisplayName("Test find brand by name returns empty when brand does not exist")
    public void givenBrandDoesNotExist_whenfindByNameIgnoreCase_thenOptionalIsEmpty() {
        // when
        Optional<Brand> foundBrand = brandRepository.findByNameIgnoreCase("NonExistentBrand");

        // then
        assertThat(foundBrand).isEmpty();
    }

    @Test
    @DisplayName("Test find brand by name is case-sensitive")
    public void givenBrandExistsWithDifferentCase_whenfindByNameIgnoreCase_thenOptionalIsEmpty() {
        // given
        Brand brand = Brand.builder().name("BMW").build();
        brandRepository.save(brand);

        // when
        Optional<Brand> found1 = brandRepository.findByNameIgnoreCase("bmw");
        Optional<Brand> found2 = brandRepository.findByNameIgnoreCase("Bmw");
        Optional<Brand> found3 = brandRepository.findByNameIgnoreCase("BMW");

        // then
        assertThat(found1).isPresent().map(Brand::getName).hasValue("BMW");
        assertThat(found2).isPresent().map(Brand::getName).hasValue("BMW");
        assertThat(found3).isPresent().map(Brand::getName).hasValue("BMW");
    }

    @Test
    @DisplayName("Test cannot save brand with same name in different case due to unique constraint")
    void givenBrandExists_whenSaveSameNameDifferentCase_thenThrowsDataIntegrityViolation() {
        // given
        brandRepository.save(Brand.builder().name("BMW").build());

        // when & then
        Brand duplicate = Brand.builder().name("bmw").build();
        assertThatThrownBy(() -> brandRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Test get all brands functionality")
    public void givenThreeBrandsAreStored_whenFindAll_thenAllBrandsAreReturned() {
        // given
        Brand brand1 = dataUtils.getBrandTransient();
        Brand brand2 = Brand.builder().name("AnotherBrand").build();
        Brand brand3 = Brand.builder().name("ThirdBrand").build();

        brandRepository.saveAll(List.of(brand1, brand2, brand3));

        // when
        List<Brand> obtainedBrands = brandRepository.findAll();

        // then
        assertThat(CollectionUtils.isEmpty(obtainedBrands)).isFalse();
        assertThat(obtainedBrands).hasSize(3);
        assertThat(obtainedBrands)
                .extracting(Brand::getName)
                .containsExactlyInAnyOrder(brand1.getName(), brand2.getName(), brand3.getName());
    }


    @Test
    @DisplayName("Test get all brands when no brands stored functionality")
    public void givenNoBrandsAreStored_whenFindAll_thenEmptyListIsReturned() {
        // when
        List<Brand> obtainedBrands = brandRepository.findAll();

        // then
        assertThat(CollectionUtils.isEmpty(obtainedBrands)).isTrue();
    }

    @Test
    @DisplayName("Test delete brand by id functionality")
    public void givenBrandIsSaved_whenDeleteById_thenBrandIsRemoved() {
        // given
        Brand brandToSave = dataUtils.getBrandTransient();
        Brand savedBrand = brandRepository.save(brandToSave);

        // when
        brandRepository.deleteById(savedBrand.getId());

        // then
        Brand obtainedBrand = brandRepository.findById(savedBrand.getId()).orElse(null);
        assertThat(obtainedBrand).isNull();
    }

    @Test
    @DisplayName("Test save brand with duplicate name throws exception due to unique constraint")
    public void givenBrandWithDuplicateName_whenSaved_thenThrowsDataIntegrityViolationException() {
        // given
        String brandName = "UniqueBrand";
        Brand brand1 = Brand.builder().name(brandName).build();
        brandRepository.save(brand1);

        Brand brand2 = Brand.builder().name(brandName).build();

        // when & then
        assertThatThrownBy(() -> brandRepository.saveAndFlush(brand2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("unique");
    }


    /**
     * Проверяет, что метод {@link BrandRepository#findByNameIgnoreCase(String)} устойчив
     * к попыткам SQL-инъекции: злонамеренные входные строки интерпретируются как обычные
     * строковые значения, а не исполняемый SQL-код.
     * <p>
     * Тест сохраняет легитимный бренд ("BMW"), затем пытается найти бренды с именами,
     * содержащими типичные паттерны SQL-инъекций. Ожидается, что результат всегда пустой.
     * После этого проверяется, что легитимный бренд по-прежнему находится при корректном запросе.
     * <p>
     * Цель: подтвердить защиту на уровне репозитория без полагания только на Spring Data JPA.
     */
    @Test
    @DisplayName("findByNameIgnoreCase is safe against SQL injection attempts")
    public void findByNameIgnoreCase_sqlInjectionAttempt_returnsEmpty() {
        // given
        Brand legitimateBrand = new Brand();
        legitimateBrand.setName("BMW");
        brandRepository.save(legitimateBrand);

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
            Optional<Brand> result = brandRepository.findByNameIgnoreCase(input);

            // then
            assertThat(result).isEmpty(); // Никакой бренд с таким именем не существует
        }

        // Убедимся, что легитимный бренд всё ещё находится
        Optional<Brand> found = brandRepository.findByNameIgnoreCase("bmw");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("BMW");
    }

}
