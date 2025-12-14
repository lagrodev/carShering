package org.example.carshering.domain.valueobject;

import org.example.carshering.fleet.domain.valueobject.Year;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Year Value Object.
 */
class YearTest {

    @Test
    void shouldCreateValidYear() {
        // Given
        int year = 2020;

        // When
        Year yearVO = Year.of(year);

        // Then
        assertThat(yearVO.getValue()).isEqualTo(2020);
    }

    @Test
    void shouldAcceptCurrentYear() {
        // Given
        int currentYear = java.time.LocalDate.now().getYear();

        // When
        Year year = Year.of(currentYear);

        // Then
        assertThat(year.getValue()).isEqualTo(currentYear);
    }

    @Test
    void shouldAcceptNextYear() {
        // Given
        int nextYear = java.time.LocalDate.now().getYear() + 1;

        // When
        Year year = Year.of(nextYear);

        // Then
        assertThat(year.getValue()).isEqualTo(nextYear);
    }

    @Test
    void shouldThrowExceptionWhenYearTooOld() {
        // Given - before first automobile
        int year = 1885;

        // When & Then
        assertThatThrownBy(() -> Year.of(year))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be less than 1886");
    }

    @Test
    void shouldThrowExceptionWhenYearInFuture() {
        // Given - 2 years in future
        int futureYear = java.time.LocalDate.now().getYear() + 2;

        // When & Then
        assertThatThrownBy(() -> Year.of(futureYear))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be greater than");
    }

    @Test
    void shouldAcceptMinimumValidYear() {
        // Given - first automobile year
        int year = 1886;

        // When
        Year yearVO = Year.of(year);

        // Then
        assertThat(yearVO.getValue()).isEqualTo(1886);
    }

    @Test
    void shouldCalculateAge() {
        // Given
        int currentYear = java.time.LocalDate.now().getYear();
        Year year = Year.of(currentYear - 5);

        // When
        int age = year.getAge();

        // Then
        assertThat(age).isEqualTo(5);
    }

    @Test
    void shouldIdentifyNewCar() {
        // Given - current year
        int currentYear = java.time.LocalDate.now().getYear();
        Year year = Year.of(currentYear);

        // When & Then
        assertThat(year.isNew()).isTrue();
        assertThat(year.isOld()).isFalse();
    }

    @Test
    void shouldIdentifyOldCar() {
        // Given - 15 years old
        int currentYear = java.time.LocalDate.now().getYear();
        Year year = Year.of(currentYear - 15);

        // When & Then
        assertThat(year.isOld()).isTrue();
        assertThat(year.isNew()).isFalse();
    }

    @Test
    void shouldNotBeNewAfterOneYear() {
        // Given - 2 years old
        int currentYear = java.time.LocalDate.now().getYear();
        Year year = Year.of(currentYear - 2);

        // When & Then
        assertThat(year.isNew()).isFalse();
    }

    @Test
    void shouldNotBeOldBeforeTenYears() {
        // Given - 9 years old
        int currentYear = java.time.LocalDate.now().getYear();
        Year year = Year.of(currentYear - 9);

        // When & Then
        assertThat(year.isOld()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {1900, 1950, 2000, 2010, 2020, 2024, 2025})
    void shouldAcceptValidYears(int validYear) {
        // Adjust if year is in future
        int currentYear = java.time.LocalDate.now().getYear();
        if (validYear > currentYear + 1) {
            return; // Skip future years
        }

        // When
        Year year = Year.of(validYear);

        // Then
        assertThat(year.getValue()).isEqualTo(validYear);
    }

    @Test
    void shouldBeEqualWhenYearsAreSame() {
        // Given
        Year year1 = Year.of(2020);
        Year year2 = Year.of(2020);

        // Then
        assertThat(year1).isEqualTo(year2);
        assertThat(year1.hashCode()).isEqualTo(year2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenYearsDiffer() {
        // Given
        Year year1 = Year.of(2020);
        Year year2 = Year.of(2021);

        // Then
        assertThat(year1).isNotEqualTo(year2);
    }

    @Test
    void shouldReturnValueInToString() {
        // Given
        Year year = Year.of(2020);

        // When
        String result = year.toString();

        // Then
        assertThat(result).isEqualTo("2020");
    }

    @Test
    void shouldCalculateCorrectAgeForCurrentYear() {
        // Given
        int currentYear = java.time.LocalDate.now().getYear();
        Year year = Year.of(currentYear);

        // When
        int age = year.getAge();

        // Then
        assertThat(age).isEqualTo(0);
    }
}