package org.example.carshering.repository;

import org.example.carshering.entity.Car;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(DataUtils.class)
@ActiveProfiles("test")
public class CarRepositoryTest extends AbstractRepositoryTest {

    private final Pageable pageable = PageRequest.of(0, 10);
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private DataUtils dataUtils;

    @BeforeEach
    void setUp() {
        carRepository.deleteAll();
    }

    @Test
    @DisplayName("Test save car functionality")
    public void givenCarObject_whenSave_thenCarIsCreated() {

        //given
        Car carToSave = dataUtils.getJohnDoeTransient();


        // when
        Car saveCar = carRepository.save(carToSave);

        //then
        assertThat(carToSave).isNotNull();
        assertThat(saveCar).isNotNull();
        assertThat(saveCar.getId()).isNotNull();

    }

    @Test
    @DisplayName("Test update car functionality")
    public void givenCarToUpdate_whenSave_thenCarIsChanged() {


        // given

        String updatedGosNumber = "9999";

        Car carToSave = dataUtils.getJohnDoeTransient();
        carRepository.save(carToSave);
        // when
        Car carToUpdate = carRepository.findById(carToSave.getId())
                .orElse(null);
        carToUpdate.setGosNumber(updatedGosNumber);
        Car updateCar = carRepository.save(carToUpdate);
        // then
        assertThat(updateCar).isNotNull();
        assertThat(updateCar.getGosNumber()).isEqualTo(updatedGosNumber);
    }

    @Test
    @DisplayName("Test get car by id functionality")
    public void givenCarCreated_whenFindById_thenCarIsReturned() {


        Car carToSave = dataUtils.getJohnDoeTransient();

        carRepository.save(carToSave);

        // when
        Car obtainedCar = carRepository.findById(carToSave.getId())
                .orElse(null);

        //then

        assertThat(obtainedCar).isNotNull();
        assertThat(obtainedCar.getGosNumber()).isEqualTo("1123");

    }

    @Test
    @DisplayName("Test car not found functionality")
    public void givenCarIsNotCreated_whenGetById_thenOptionalIsEmpty() {

        //given

        // when
        Car obtainedCar = carRepository.findById(1L)
                .orElse(null);

        //then

        assertThat(obtainedCar).isNull();

    }

    @Test
    @DisplayName("Test get all cars functionality")
    public void givenThreeCarsAreStores_whenFindAll_thenAllCarsAreReturned() {

        // given
        Car car1 = dataUtils.getJohnDoeTransient();
        Car car2 = dataUtils.getFrankJonesTransient();
        Car car3 = dataUtils.getMikeSmithTransient();

        carRepository.saveAll(List.of(car1, car2, car3));


        // when
        List<Car> obtainedCars = carRepository.findAll();

        // then
        assertThat(CollectionUtils.isEmpty(obtainedCars)).isFalse();
    }

    @Test
    @DisplayName("Test get all cars when no cars stored functionality")
    public void givenNoCarsAreStored_whenFindAll_thenEmptyListIsReturned() {

        // given

        // when
        List<Car> obtainedCars = carRepository.findAll();

        // then
        assertThat(CollectionUtils.isEmpty(obtainedCars)).isTrue();
    }


    @Test
    @DisplayName("Test delete car by id functionality")
    public void givenCarIsSaved_whenDeleteById_thenCarIsRemoved() {
        // given
        Car carToSave = dataUtils.getJohnDoeTransient();
        carRepository.save(carToSave);

        // when
        carRepository.deleteById(carToSave.getId());

        // then
        Car obtainedCar = carRepository.findById(carToSave.getId()).orElse(null);
        assertThat(obtainedCar).isNull();
    }

    @Test
    @DisplayName("Test exists car by gos number functionality")
    public void existsCarByGosNumber_ReturnsTrueWhenCarExists() {

        // given
        Car carToSave = dataUtils.getJohnDoeTransient();

        carRepository.save(carToSave);

        // when
        boolean exists = carRepository.existsByGosNumber(carToSave.getGosNumber());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Test exists car by vin functionality")
    public void existsCarByVin_ReturnsTrueWhenCarExists() {
        // given
        Car carToSave = dataUtils.getJohnDoeTransient();

        carRepository.save(carToSave);

        // when
        boolean exists = carRepository.existsByVin(carToSave.getVin());

        // then
        assertThat(exists).isTrue();
    }





    @Test
    @DisplayName("findByFilter returns all cars when all parameters are null")
    void givenAllParamsNull_whenFindByFilter_thenAllCarsReturned() {
        // given
        var car1 = carRepository.save(dataUtils.getJohnDoeTransient());
        var car2 = carRepository.save(dataUtils.getFrankJonesTransient());
        var car3 = carRepository.save(dataUtils.getMikeSmithTransient());

        // when
        Page<Car> result = carRepository.findByFilter(null, null, null, null, null, null, null, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
                .extracting(Car::getId)
                .containsExactlyInAnyOrder(car1.getId(), car2.getId(), car3.getId());
    }


    @Test
    @DisplayName("findByFilter filters cars by brand correctly")
    void givenBrandFilter_whenFindByFilter_thenOnlyBrandMatchesReturned() {
        // given
        var car1 = carRepository.save(dataUtils.getCarWithSpecificAttributes("V1", "G1", 2020, "BMW", "X5", "SUV", "LUXURY", "ACTIVE"));
        var car2 = carRepository.save(dataUtils.getCarWithSpecificAttributes("V2", "G2", 2021, "BMW", "X3", "SUV", "LUXURY", "ACTIVE"));
        var car3 = carRepository.save(dataUtils.getCarWithSpecificAttributes("V3", "G3", 2022, "AUDI", "A4", "SEDAN", "STANDARD", "ACTIVE"));

        // when
        Page<Car> result = carRepository.findByFilter(List.of("BMW"), null, null, null, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Car::getId)
                .containsExactlyInAnyOrder(car1.getId(), car2.getId());
        assertThat(result.getContent())
                .noneMatch(car -> car.getId().equals(car3.getId()));
    }

    @Test
    @DisplayName("findByFilter filters cars by model correctly")
    void givenModelFilter_whenFindByFilter_thenOnlyModelMatchesReturned() {
        // given
        var matchingCar = carRepository.save(dataUtils.getCarWithSpecificAttributes("V1", "G1", 2020, "TOYOTA", "CAMRY", "SEDAN", "STANDARD", "ACTIVE"));
        var nonMatchingCar = carRepository.save(dataUtils.getCarWithSpecificAttributes("V2", "G2", 2021, "TOYOTA", "COROLLA", "SEDAN", "STANDARD", "ACTIVE"));

        // when
        Page<Car> result = carRepository.findByFilter(null, List.of("CAMRY"), null, null, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(matchingCar.getId());
        assertThat(result.getContent())
                .noneMatch(car -> car.getId().equals(nonMatchingCar.getId()));
    }

    @Test
    @DisplayName("findByFilter filters cars by year range (minYear and maxYear)")
    void givenYearRange_whenFindByFilter_thenOnlyCarsWithinRangeReturned() {
        // given
        var car2020 = carRepository.save(dataUtils.getCarWithSpecificAttributes("V1", "G1", 2020, "BMW", "X5", "SUV", "LUXURY", "ACTIVE"));
        var car2022 = carRepository.save(dataUtils.getCarWithSpecificAttributes("V2", "G2", 2022, "AUDI", "A4", "SEDAN", "STANDARD", "ACTIVE"));
        var car2025 = carRepository.save(dataUtils.getCarWithSpecificAttributes("V3", "G3", 2025, "MERCEDES", "E", "SEDAN", "LUXURY", "ACTIVE"));

        // when
        Page<Car> result = carRepository.findByFilter(null, null, 2021, 2023, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(car2022.getId());
        assertThat(result.getContent())
                .extracting(Car::getYearOfIssue)
                .containsExactly(2022);
    }


    @Test
    @DisplayName("findByFilter filters cars by body type correctly")
    void givenBodyTypeFilter_whenFindByFilter_thenOnlyMatchingBodyTypeReturned() {
        // given
        var sedan = carRepository.save(dataUtils.getCarWithSpecificAttributes("V1", "G1", 2020, "BMW", "X5", "SEDAN", "LUXURY", "ACTIVE"));
        var suv = carRepository.save(dataUtils.getCarWithSpecificAttributes("V2", "G2", 2021, "AUDI", "Q7", "SUV", "LUXURY", "ACTIVE"));

        // when
        Page<Car> result = carRepository.findByFilter(null, null, null, null, "SEDAN", null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(sedan.getId());
        assertThat(result.getContent().get(0).getModel().getBodyType()).isEqualTo("SEDAN");
    }


    @Test
    @DisplayName("findByFilter filters cars by class correctly")
    void givenCarClassFilter_whenFindByFilter_thenOnlyMatchingClassesReturned() {
        // given
        var luxury1 = carRepository.save(dataUtils.getCarWithSpecificAttributes("V1", "G1", 2020, "BMW", "X5", "SUV", "LUXURY", "ACTIVE"));
        var luxury2 = carRepository.save(dataUtils.getCarWithSpecificAttributes("V2", "G2", 2021, "MERCEDES", "E", "SEDAN", "LUXURY", "ACTIVE"));
        var standard = carRepository.save(dataUtils.getCarWithSpecificAttributes("V3", "G3", 2022, "TOYOTA", "COROLLA", "SEDAN", "STANDARD", "ACTIVE"));

        // when
        Page<Car> result = carRepository.findByFilter(null, null, null, null, null, List.of("LUXURY"), null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Car::getId)
                .containsExactlyInAnyOrder(luxury1.getId(), luxury2.getId());
        assertThat(result.getContent())
                .noneMatch(car -> car.getId().equals(standard.getId()));
    }

    @Test
    @DisplayName("findByFilter filters cars by state correctly")
    void givenCarStateFilter_whenFindByFilter_thenOnlyMatchingStatesReturned() {
        // given
        var activeCar = carRepository.save(dataUtils.getCarWithSpecificAttributes("V1", "G1", 2020, "BMW", "X5", "SUV", "LUXURY", "ACTIVE"));
        var inactiveCar = carRepository.save(dataUtils.getCarWithSpecificAttributes("V2", "G2", 2021, "AUDI", "A4", "SEDAN", "STANDARD", "INACTIVE"));

        // when
        Page<Car> result = carRepository.findByFilter(null, null, null, null, null, null, List.of("ACTIVE"), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(activeCar.getId());
        assertThat(result.getContent().get(0).getState().getStatus()).isEqualTo("ACTIVE");
    }


    @Test
    @DisplayName("findByFilter returns empty result when no filters match")
    void givenNoMatchingParams_whenFindByFilter_thenEmptyResultReturned() {
        // given
        var existingCar = carRepository.save(dataUtils.getJohnDoeTransient());

        // when
        Page<Car> result = carRepository.findByFilter(
                List.of("NONEXISTENT_BRAND"),
                List.of("NONEXISTENT_MODEL"),
                2030, 2035,
                "HATCHBACK",
                List.of("NONEXISTENT_CLASS"),
                List.of("OLDSTATE"),
                pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }


    @Test
    @DisplayName("findByFilter correctly combines multiple filter parameters")
    void givenMultipleFilters_whenFindByFilter_thenCorrectSubsetReturned() {
        // given
        var targetCar = carRepository.save(dataUtils.getCarWithSpecificAttributes("JOHNDOE", "G1", 2020, "brand", "nameModel", "SEDAN", "carClass", "NEWSTATE"));
        var differentBrand = carRepository.save(dataUtils.getCarWithSpecificAttributes("OTHER1", "G2", 2020, "otherBrand", "nameModel", "SEDAN", "carClass", "NEWSTATE"));
        var differentYear = carRepository.save(dataUtils.getCarWithSpecificAttributes("OTHER2", "G3", 2025, "brand", "nameModel", "SEDAN", "carClass", "NEWSTATE"));

        // when
        Page<Car> result = carRepository.findByFilter(
                List.of("brand"),
                List.of("nameModel"),
                2019, 2021,
                "SEDAN",
                List.of("carClass"),
                List.of("NEWSTATE"),
                pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(targetCar.getId());
    }


    // <!-- Add more utility methods as needed --!>
    // <!-- new methods --!>

    @Test
    @DisplayName("findByFilter handles minYear equals maxYear correctly")
    void givenMinYearEqualsMaxYear_whenFindByFilter_thenOnlyExactYearCarsReturned() {
        // given
        var car2021 = carRepository.save(dataUtils.getCarWithSpecificAttributes("VIN2021", "G2021", 2021, "BMW", "X5", "SUV", "LUXURY", "ACTIVE"));
        var car2020 = carRepository.save(dataUtils.getCarWithSpecificAttributes("VIN2020", "G2020", 2020, "AUDI", "A4", "SEDAN", "STANDARD", "ACTIVE"));

        // when
        Page<Car> result = carRepository.findByFilter(null, null, 2021, 2021, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(car2021.getId());
    }

    @Test
    @DisplayName("findByFilter with year range where minYear > maxYear returns empty")
    void givenMinYearGreaterThanMaxYear_whenFindByFilter_thenEmptyResult() {
        // given
        carRepository.save(dataUtils.getJohnDoeTransient());

        // when
        Page<Car> result = carRepository.findByFilter(null, null, 2025, 2020, null, null, null, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findByFilter respects pagination correctly")
    void givenMoreThanPageSizeCars_whenFindByFilter_thenOnlyFirstPageReturned() {
        // given
        Pageable smallPage = PageRequest.of(0, 2);
        var car1 = carRepository.save(dataUtils.getJohnDoeTransient());
        var car2 = carRepository.save(dataUtils.getFrankJonesTransient());
        var car3 = carRepository.save(dataUtils.getMikeSmithTransient());

        // when
        Page<Car> result = carRepository.findByFilter(null, null, null, null, null, null, null, smallPage);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        // Optional: check that it's the first two (order may vary unless sorted)
    }

    @Test
    @DisplayName("findByFilter with non-matching bodyType returns empty")
    void givenNonMatchingBodyType_whenFindByFilter_thenEmptyResult() {
        // given
        carRepository.save(dataUtils.getJohnDoeTransient()); // bodyType = "SEDAN"

        // when
        Page<Car> result = carRepository.findByFilter(null, null, null, null, "HATCHBACK", null, null, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }


    @Test
    @DisplayName("findByFilter returns empty list when list contains only empty string")
    void givenEmptyStringInList_whenFindByFilter_thenEmptyResultReturned() {
        // given
        carRepository.save(dataUtils.getJohnDoeTransient());

        // when
        Page<Car> result = carRepository.findByFilter(
                List.of(""), List.of(""), null, null, "", List.of(""), List.of(""), pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }


    @Test
    @DisplayName("findByFilter returns cars exactly on boundary years (minYear == carYear)")
    void givenExactMinYearMatch_whenFindByFilter_thenCarReturned() {
        // given
        var car2020 = carRepository.save(dataUtils.getJohnDoeTransient()); // year = 2020

        // when
        Page<Car> result = carRepository.findByFilter(null, null, 2020, null, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(car2020.getId());
    }

    @Test
    @DisplayName("findByFilter returns cars exactly on boundary years (maxYear == carYear)")
    void givenExactMaxYearMatch_whenFindByFilter_thenCarReturned() {
        // given
        var car2020 = carRepository.save(dataUtils.getJohnDoeTransient());

        // when
        Page<Car> result = carRepository.findByFilter(null, null, null, 2020, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(car2020.getId());
    }


    @Test
    @DisplayName("findByFilter handles mixed filters (some null, some with values)")
    void givenPartialFilters_whenFindByFilter_thenFilteredSubsetReturned() {
        // given
        var matchingCar = carRepository.save(dataUtils.getCarWithSpecificAttributes("V1", "G1", 2020, "BMW", "X5", "SUV", "LUXURY", "ACTIVE"));
        var nonMatchingBrand = carRepository.save(dataUtils.getCarWithSpecificAttributes("V2", "G2", 2020, "AUDI", "X5", "SUV", "LUXURY", "ACTIVE"));

        // when
        Page<Car> result = carRepository.findByFilter(List.of("BMW"), null, null, null, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(matchingCar.getId());
    }


    @Test
    @DisplayName("findByFilter returns only cars within min/max year boundaries")
    void givenYearBoundaries_whenFindByFilter_thenOnlyInRangeReturned() {
        // given
        var oldCar = carRepository.save(dataUtils.getOldCarTransient());      // 1999
        var validCar = carRepository.save(dataUtils.getJohnDoeTransient());   // 2020
        var futureCar = carRepository.save(dataUtils.getFutureCarTransient()); // 2030

        // when
        Page<Car> result = carRepository.findByFilter(null, null, 2000, 2025, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(validCar.getId());
        assertThat(result.getContent().get(0).getYearOfIssue()).isEqualTo(2020);
    }

    @Test
    @DisplayName("findByFilter returns empty page when page exceeds total results")
    void givenPageExceedsResults_whenFindByFilter_thenEmptyPageReturned() {
        // given
        carRepository.save(dataUtils.getJohnDoeTransient());

        Pageable pageRequest = PageRequest.of(5, 10);

        // when
        Page<Car> result = carRepository.findByFilter(null, null, null, null, null, null, null, pageRequest);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findByFilter is case-sensitive for brand and model names")
    void givenDifferentCaseBrandOrModel_whenFindByFilter_thenEmptyResult() {
        // given
        carRepository.save(dataUtils.getCarWithSpecificAttributes("V1", "G1", 2020, "BMW", "X5", "SUV", "LUXURY", "ACTIVE"));

        // when
        Page<Car> result = carRepository.findByFilter(
                List.of("bmw"), List.of("x5"), null, null, null, null, null, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    //<!-- end new methods --!>
    @Test
    @DisplayName("findByFilter returns all cars when all filter parameters are null")
    void givenAllFiltersNull_whenFindByFilter_thenAllCarsReturned() {
        // given
        var car1 = carRepository.save(dataUtils.getJohnDoeTransient());
        var car2 = carRepository.save(dataUtils.getFrankJonesTransient());
        var car3 = carRepository.save(dataUtils.getMikeSmithTransient());

        // when
        Page<Car> result = carRepository.findByFilter(
                null, null, null, null, null, null, null, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
                .extracting(Car::getId)
                .containsExactlyInAnyOrder(car1.getId(), car2.getId(), car3.getId());
    }

    @Test
    @DisplayName("findByFilter returns empty page when year range excludes all cars")
    void givenYearRangeOutsideAllCars_whenFindByFilter_thenEmptyResultReturned() {
        // given
        carRepository.save(dataUtils.getJohnDoeTransient());
        carRepository.save(dataUtils.getFrankJonesTransient());

        // when
        Page<Car> result = carRepository.findByFilter(null, null, 2030, 2040, null, null, null, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }


    @Test
    @DisplayName("findByFilter returns only cars matching body type when others differ")
    void givenDifferentBodyTypes_whenFindByFilter_thenOnlyMatchingReturned() {
        // given
        var sedan = carRepository.save(dataUtils.getJohnDoeTransient()); // SEDAN
        var hatchback = carRepository.save(dataUtils.getOldCarTransient()); // HATCHBACK

        // when
        Page<Car> result = carRepository.findByFilter(null, null, null, null, "SEDAN", null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(sedan.getId());
    }


    @Test
    @DisplayName("findByFilter returns empty page when pageable exceeds total number of cars")
    void givenPageExceedsTotalCars_whenFindByFilter_thenEmptyPageReturned() {
        // given
        carRepository.save(dataUtils.getJohnDoeTransient());
        Pageable pageRequest = PageRequest.of(5, 10); // за пределами диапазона

        // when
        Page<Car> result = carRepository.findByFilter(
                null, null, null, null, null, null, null, pageRequest);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findByFilter correctly filters by combination of brand and year range")
    void givenBrandAndYearRange_whenFindByFilter_thenOnlyMatchingCarsReturned() {
        // given
        var bmw2020 = carRepository.save(dataUtils.getCarWithSpecificAttributes("JOHNDOE", "G1", 2020, "BMW", "X5", "SUV", "LUXURY", "ACTIVE"));
        var audi2019 = carRepository.save(dataUtils.getCarWithSpecificAttributes("OLD", "G2", 2019, "AUDI", "A4", "SEDAN", "STANDARD", "ACTIVE"));

        // when
        Page<Car> result = carRepository.findByFilter(List.of("BMW"), null, 2019, 2025, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(bmw2020.getId());
    }

    @Test
    @DisplayName("findByFilter returns empty result when all params non-null but no matches found")
    void givenAllParamsNonMatching_whenFindByFilter_thenEmptyResultReturned() {
        // given
        carRepository.save(dataUtils.getJohnDoeTransient());

        // when
        Page<Car> result = carRepository.findByFilter(
                List.of("NONEXISTENT"), List.of("OTHER"), 1990, 1991, "SUV",
                List.of("NOCLASS"), List.of("OLDSTATE"), pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findByFilter handles mix of null and non-null filters correctly")
    void givenMixedNullAndNonNullFilters_whenFindByFilter_thenOnlyCorrectSubsetReturned() {
        // given
        var targetCar = carRepository.save(dataUtils.getCarWithSpecificAttributes("JOHNDOE", "G1", 2020, "BMW", "X5", "SUV", "LUXURY", "ACTIVE"));
        var wrongYear = carRepository.save(dataUtils.getCarWithSpecificAttributes("OLD", "G2", 2018, "BMW", "X5", "SUV", "LUXURY", "ACTIVE"));

        // when
        Page<Car> result = carRepository.findByFilter(List.of("BMW"), null, 2019, 2021, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(targetCar.getId());
    }




    // <!-- Add more tests as needed --!>

    @Test
    @DisplayName("findByFilter returns only cars with specified brand, excluding others")
    void givenBrandFilter_whenFindByFilter_thenOnlyMatchingBrandsReturnedAndOthersExcluded() {
        // given
        var bmw1 = carRepository.save(dataUtils.getCarWithSpecificAttributes("V1", "G1", 2020, "BMW", "X5", "SUV", "LUXURY", "ACTIVE"));
        var bmw2 = carRepository.save(dataUtils.getCarWithSpecificAttributes("V2", "G2", 2021, "BMW", "X3", "SUV", "LUXURY", "ACTIVE"));
        var audi = carRepository.save(dataUtils.getCarWithSpecificAttributes("V3", "G3", 2022, "AUDI", "A4", "SEDAN", "STANDARD", "ACTIVE"));

        // when
        Page<Car> result = carRepository.findByFilter(
                List.of("BMW"), null, null, null, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Car::getId)
                .containsExactlyInAnyOrder(bmw1.getId(), bmw2.getId());

        // Or by VIN (if you prefer VIN as identifier):
        assertThat(result.getContent())
                .extracting(Car::getVin)
                .containsExactlyInAnyOrder(bmw1.getVin(), bmw2.getVin());

        // Ensure AUDI is excluded
        assertThat(result.getContent())
                .noneMatch(car -> car.getId().equals(audi.getId()));
    }

    @Test
    @DisplayName("findByFilter with brand and model returns only exact matches")
    void givenBrandAndModelFilter_whenFindByFilter_thenOnlyExactMatchesReturned() {
        // given
        var bmwX5 = carRepository.save(dataUtils.getCarWithSpecificAttributes("V1", "G1", 2020, "BMW", "X5", "SUV", "LUXURY", "ACTIVE"));
        var bmwX3 = carRepository.save(dataUtils.getCarWithSpecificAttributes("V2", "G2", 2021, "BMW", "X3", "SUV", "LUXURY", "ACTIVE")); // ← different model
        var audiA4 = carRepository.save(dataUtils.getCarWithSpecificAttributes("V3", "G3", 2022, "AUDI", "A4", "SEDAN", "STANDARD", "ACTIVE"));

        // when
        Page<Car> result = carRepository.findByFilter(
                List.of("BMW"), List.of("X5"), null, null, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getVin()).isEqualTo("V1");

        // Ensure X3 and A4 are excluded
        assertThat(result.getContent())
                .extracting(car -> car.getModel().getModel().getName())
                .containsOnly("X5");
    }

    @Test
    @DisplayName("findByFilter by car class returns only cars of the specified class")
    void givenCarClassFilter_whenFindByFilter_thenOnlyCarsOfThatClassReturned() {
        // given
        var luxury1 = carRepository.save(dataUtils.getCarWithSpecificAttributes("V1", "G1", 2020, "BMW", "X5", "SUV", "LUXURY", "ACTIVE"));
        var luxury2 = carRepository.save(dataUtils.getCarWithSpecificAttributes("V2", "G2", 2021, "MERCEDES", "E", "SEDAN", "LUXURY", "ACTIVE"));
        var standard = carRepository.save(dataUtils.getCarWithSpecificAttributes("V3", "G3", 2022, "TOYOTA", "COROLLA", "SEDAN", "STANDARD", "ACTIVE"));

        // when
        Page<Car> result = carRepository.findByFilter(
                null, null, null, null, null, List.of("LUXURY"), null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(car -> car.getModel().getCarClass().getName())
                .allMatch(cls -> cls.equals("LUXURY"));

        // Verify that STANDARD car is not included
        assertThat(result.getContent())
                .extracting(Car::getVin)
                .doesNotContain("V3");
    }



    /**
     * Проверяет устойчивость метода {@link CarRepository#findByFilter} к SQL-инъекциям
     * через все строковые и коллекционные параметры фильтрации: бренды, модели, типы кузова,
     * классы автомобилей и состояния.
     * <p>
     * Каждый параметр, принимающий пользовательский ввод (в виде {@code String} или {@code List<String>}),
     * потенциально уязвим, если реализован через конкатенацию SQL. Тест подаёт известные векторы атак
     * в каждый параметр по отдельности и убеждается, что:
     * <ul>
     *   <li>Результат всегда пуст (никакие данные не утекают);</li>
     *   <li>Легитимный автомобиль не возвращается при злонамеренном запросе;</li>
     *   <li>Запрос не вызывает SQL-исключений (т.е. не ломается и не исполняет код).</li>
     * </ul>
     * <p>
     * Контекст безопасности:
     * <ul>
     *   <li>Методы вроде {@code findById(Long)} используют типизированные параметры — инъекция невозможна.</li>
     *   <li>Запросы без параметров (например, {@code findAllActive()}) неуязвимы по определению.</li>
     *   <li>Все фильтры с пользовательским вводом находятся под контролем: либо через Spring Data JPA
     *       (параметризованные именованные запросы), либо через Criteria API / QueryDSL.</li>
     * </ul>
     * <p>
     * Тест не проверяет логику фильтрации — только её безопасность.
     */
    @Test
    @DisplayName("findByFilter is safe against SQL injection in all string-based filter parameters")
    void findByFilter_sqlInjectionAttempts_returnEmptyAndDoNotExposeData() {
        // given
        Car legitimateCar = carRepository.save(dataUtils.getJohnDoeTransient()); // has brand="brand", model="nameModel", bodyType="SEDAN", etc.

        List<String> injectionAttempts = Arrays.asList(
                "' OR '1'='1",
                "'; DROP TABLE cars; --",
                "admin'--",
                "\" OR \"\"=\"",
                "'; SELECT * FROM information_schema.tables; --",
                "1' UNION SELECT null,null,null,null,null,null,null--",
                "'; EXEC xp_cmdshell('ping 127.0.0.1') --",
                "/*",
                "*/ UNION SELECT * FROM cars; /*",
                "\\'; DELETE FROM cars; --",
                "x' AND 1=1; --",
                "x' OR 'x'='x"
        );

        // when & then
        for (String payload : injectionAttempts) {

            // 1. brands
            Page<Car> resultByBrand = carRepository.findByFilter(
                    List.of(payload), null, null, null, null, null, null, pageable);
            assertThat(resultByBrand).isEmpty();
            assertThat(resultByBrand.getContent()).doesNotContain(legitimateCar);

            // 2. models
            Page<Car> resultByModel = carRepository.findByFilter(
                    null, List.of(payload), null, null, null, null, null, pageable);
            assertThat(resultByModel).isEmpty();
            assertThat(resultByModel.getContent()).doesNotContain(legitimateCar);

            // 3. bodyType
            Page<Car> resultByBodyType = carRepository.findByFilter(
                    null, null, null, null, payload, null, null, pageable);
            assertThat(resultByBodyType).isEmpty();
            assertThat(resultByBodyType.getContent()).doesNotContain(legitimateCar);

            // 4. carClasses
            Page<Car> resultByCarClass = carRepository.findByFilter(
                    null, null, null, null, null, List.of(payload), null, pageable);
            assertThat(resultByCarClass).isEmpty();
            assertThat(resultByCarClass.getContent()).doesNotContain(legitimateCar);

            // 5. carStates
            Page<Car> resultByState = carRepository.findByFilter(
                    null, null, null, null, null, null, List.of(payload), pageable);
            assertThat(resultByState).isEmpty();
            assertThat(resultByState.getContent()).doesNotContain(legitimateCar);
        }
    }


    /**
     * Проверяет устойчивость метода {@link CarRepository#existsByGosNumber(String)} к SQL-инъекциям.
     * <p>
     * Метод принимает единственный строковый параметр — гос. номер автомобиля. Несмотря на то,
     * что номер обычно имеет строгий формат, он всё равно поступает от клиента и может быть подменён.
     * Тест убеждается, что злонамеренные строки (включая попытки обхода через комментарии и UNION)
     * не приводят к:
     * <ul>
     *   <li>возврату {@code true} для несуществующих номеров;</li>
     *   <li>выполнению произвольного SQL;</li>
     *   <li>утечке данных или изменению состояния БД.</li>
     * </ul>
     * <p>
     * После проверки атак подтверждается, что легитимный гос. номер ("1123") по-прежнему распознаётся.
     * <p>
     * Контекст безопасности:
     * <ul>
     *   <li>Метод работает с {@code String}, но реализован через Spring Data JPA → использует
     *       параметризованные запросы → инъекция невозможна при корректной реализации.</li>
     *   <li>Все методы с примитивными или типизированными параметрами (например, {@code findById(Long)})
     *       неуязвимы; уязвимы только те, где ввод — строка от пользователя.</li>
     * </ul>
     */
    @Test
    @DisplayName("existsByGosNumber is safe against SQL injection attempts")
    void existsByGosNumber_sqlInjectionAttempts_returnFalseAndDoNotLeakData() {
        // given
        Car legitimateCar = dataUtils.getJohnDoeTransient();
        carRepository.save(legitimateCar); // gosNumber = "1123"

        List<String> injectionAttempts = Arrays.asList(
                "' OR '1'='1",
                "'; DROP TABLE cars; --",
                "1123'--",          // попытка обойти проверку, добавив коммент
                "1123' OR 'x'='x",
                "\" OR \"\"=\"",
                "1' UNION SELECT null--",
                "\\'; DELETE FROM cars; --",
                "/*",
                "*/ OR 1=1; /*"
        );

        // when & then
        for (String payload : injectionAttempts) {
            boolean result = carRepository.existsByGosNumber(payload);
            assertThat(result)
                    .as("existsByGosNumber should return false for payload: %s", payload)
                    .isFalse();
        }

        // Убедимся, что легитимный номер всё ещё работает
        assertThat(carRepository.existsByGosNumber("1123")).isTrue();
    }

    /**
     * Проверяет устойчивость метода {@link CarRepository#existsByVin(String)} к SQL-инъекциям.
     * <p>
     * VIN-номер передаётся как строка и может быть подконтролен атакующему. Тест подаёт
     * типичные векторы SQL-инъекций (включая попытки обхода через комментарии и логические условия)
     * и убеждается, что метод:
     * <ul>
     *   <li>всегда возвращает {@code false} для несуществующих/поддельных VIN;</li>
     *   <li>не выполняет внестроковый SQL;</li>
     *   <li>не вызывает исключений (что могло бы указывать на синтаксическую уязвимость).</li>
     * </ul>
     * <p>
     * После атак проверяется, что валидный VIN ("JOHNDOE") по-прежнему корректно распознаётся.
     * <p>
     * Контекст безопасности:
     * <ul>
     *   <li>Как и {@code existsByGosNumber}, метод безопасен благодаря использованию
     *       параметризованных запросов в Spring Data JPA.</li>
     *   <li>Любой метод, принимающий {@code String} от пользователя, должен быть явно протестирован —
     *       доверие без проверки недопустимо.</li>
     * </ul>
     */
    @Test
    @DisplayName("existsByVin is safe against SQL injection attempts")
    void existsByVin_sqlInjectionAttempts_returnFalseAndDoNotLeakData() {
        // given
        Car legitimateCar = dataUtils.getJohnDoeTransient();
        carRepository.save(legitimateCar); // vin = "JOHNDOE"

        List<String> injectionAttempts = Arrays.asList(
                "' OR '1'='1",
                "'; SELECT * FROM cars; --",
                "JOHNDOE'--",
                "JOHNDOE' OR '1'='1",
                "\" OR \"\"=\"",
                "x' AND 1=1; --",
                "*/ UNION SELECT * FROM cars; /*",
                "\\'; DROP DATABASE; --"
        );

        // when & then
        for (String payload : injectionAttempts) {
            boolean result = carRepository.existsByVin(payload);
            assertThat(result)
                    .as("existsByVin should return false for payload: %s", payload)
                    .isFalse();
        }

        // Проверяем, что валидный VIN всё ещё распознаётся
        assertThat(carRepository.existsByVin("JOHNDOE")).isTrue();
    }

}

