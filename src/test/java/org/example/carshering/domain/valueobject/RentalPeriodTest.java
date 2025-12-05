package org.example.carshering.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for RentalPeriod Value Object.
 */
class RentalPeriodTest {

    @Test
    void shouldCreateValidRentalPeriod() {
        // Given
        LocalDateTime start = LocalDateTime.of(2025, 12, 5, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 7, 10, 0);

        // When
        RentalPeriod period = RentalPeriod.of(start, end);

        // Then
        assertThat(period.getStartDate()).isEqualTo(start);
        assertThat(period.getEndDate()).isEqualTo(end);
        assertThat(period.getDurationMinutes()).isEqualTo(2880); // 2 days
        assertThat(period.getDurationInHours()).isEqualTo(48);
        assertThat(period.getDurationInDays()).isEqualTo(2);
    }

    @Test
    void shouldThrowExceptionWhenStartDateIsNull() {
        // Given
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // When & Then
        assertThatThrownBy(() -> RentalPeriod.of(null, end))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Rental start date cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenEndDateIsNull() {
        // Given
        LocalDateTime start = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> RentalPeriod.of(start, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Rental end date cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenEndDateIsBeforeStartDate() {
        // Given
        LocalDateTime start = LocalDateTime.of(2025, 12, 7, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 5, 10, 0);

        // When & Then
        assertThatThrownBy(() -> RentalPeriod.of(start, end))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be after start date");
    }

    @Test
    void shouldThrowExceptionWhenEndDateEqualsStartDate() {
        // Given
        LocalDateTime start = LocalDateTime.of(2025, 12, 5, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 5, 10, 0);

        // When & Then
        assertThatThrownBy(() -> RentalPeriod.of(start, end))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be after start date");
    }

    @Test
    void shouldThrowExceptionWhenDurationLessThanOneHour() {
        // Given - 30 minutes
        LocalDateTime start = LocalDateTime.of(2025, 12, 5, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 5, 10, 30);

        // When & Then
        assertThatThrownBy(() -> RentalPeriod.of(start, end))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Minimum rental duration: 60 minutes");
    }

    @Test
    void shouldAcceptExactlyOneHourDuration() {
        // Given - exactly 1 hour
        LocalDateTime start = LocalDateTime.of(2025, 12, 5, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 5, 11, 0);

        // When
        RentalPeriod period = RentalPeriod.of(start, end);

        // Then
        assertThat(period.getDurationMinutes()).isEqualTo(60);
        assertThat(period.getDurationInHours()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenDurationExceedsMaxDays() {
        // Given - 91 days
        LocalDateTime start = LocalDateTime.of(2025, 12, 5, 10, 0);
        LocalDateTime end = start.plusDays(91);

        // When & Then
        assertThatThrownBy(() -> RentalPeriod.of(start, end))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Maximum rental duration: 90 days");
    }

    @Test
    void shouldAcceptExactlyMaxDurationDays() {
        // Given - exactly 90 days
        LocalDateTime start = LocalDateTime.of(2025, 12, 5, 10, 0);
        LocalDateTime end = start.plusDays(90);

        // When
        RentalPeriod period = RentalPeriod.of(start, end);

        // Then
        assertThat(period.getDurationInDays()).isEqualTo(90);
    }

    @Test
    void shouldDetectOverlappingPeriods() {
        // Given
        RentalPeriod period1 = RentalPeriod.of(
            LocalDateTime.of(2025, 12, 5, 10, 0),
            LocalDateTime.of(2025, 12, 7, 10, 0)
        );
        RentalPeriod period2 = RentalPeriod.of(
            LocalDateTime.of(2025, 12, 6, 10, 0),
            LocalDateTime.of(2025, 12, 8, 10, 0)
        );

        // When & Then
        assertThat(period1.overlaps(period2)).isTrue();
        assertThat(period2.overlaps(period1)).isTrue();
    }

    @Test
    void shouldDetectNonOverlappingPeriods() {
        // Given
        RentalPeriod period1 = RentalPeriod.of(
            LocalDateTime.of(2025, 12, 5, 10, 0),
            LocalDateTime.of(2025, 12, 7, 10, 0)
        );
        RentalPeriod period2 = RentalPeriod.of(
            LocalDateTime.of(2025, 12, 8, 10, 0),
            LocalDateTime.of(2025, 12, 10, 10, 0)
        );

        // When & Then
        assertThat(period1.overlaps(period2)).isFalse();
        assertThat(period2.overlaps(period1)).isFalse();
    }

    @Test
    void shouldDetectOverlapWhenPeriodsShareBoundary() {
        // Given - period1 ends exactly when period2 starts
        RentalPeriod period1 = RentalPeriod.of(
            LocalDateTime.of(2025, 12, 5, 10, 0),
            LocalDateTime.of(2025, 12, 7, 10, 0)
        );
        RentalPeriod period2 = RentalPeriod.of(
            LocalDateTime.of(2025, 12, 7, 10, 0),
            LocalDateTime.of(2025, 12, 9, 10, 0)
        );

        // When & Then
        assertThat(period1.overlaps(period2)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenOverlapWithNull() {
        // Given
        RentalPeriod period = RentalPeriod.of(
            LocalDateTime.of(2025, 12, 5, 10, 0),
            LocalDateTime.of(2025, 12, 7, 10, 0)
        );

        // When & Then
        assertThat(period.overlaps(null)).isFalse();
    }

    @Test
    void shouldDetectDateContainedInPeriod() {
        // Given
        RentalPeriod period = RentalPeriod.of(
            LocalDateTime.of(2025, 12, 5, 10, 0),
            LocalDateTime.of(2025, 12, 7, 10, 0)
        );
        LocalDateTime dateInside = LocalDateTime.of(2025, 12, 6, 10, 0);

        // When & Then
        assertThat(period.contains(dateInside)).isTrue();
    }

    @Test
    void shouldDetectDateNotContainedInPeriod() {
        // Given
        RentalPeriod period = RentalPeriod.of(
            LocalDateTime.of(2025, 12, 5, 10, 0),
            LocalDateTime.of(2025, 12, 7, 10, 0)
        );
        LocalDateTime dateOutside = LocalDateTime.of(2025, 12, 8, 10, 0);

        // When & Then
        assertThat(period.contains(dateOutside)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenContainsNull() {
        // Given
        RentalPeriod period = RentalPeriod.of(
            LocalDateTime.of(2025, 12, 5, 10, 0),
            LocalDateTime.of(2025, 12, 7, 10, 0)
        );

        // When & Then
        assertThat(period.contains(null)).isFalse();
    }

    @Test
    void shouldIdentifyLongTermRental() {
        // Given - 10 days
        RentalPeriod period = RentalPeriod.of(
            LocalDateTime.of(2025, 12, 5, 10, 0),
            LocalDateTime.of(2025, 12, 15, 10, 0)
        );

        // When & Then
        assertThat(period.isLongTerm()).isTrue();
        assertThat(period.isShortTerm()).isFalse();
    }

    @Test
    void shouldIdentifyShortTermRental() {
        // Given - 5 hours
        RentalPeriod period = RentalPeriod.of(
            LocalDateTime.of(2025, 12, 5, 10, 0),
            LocalDateTime.of(2025, 12, 5, 15, 0)
        );

        // When & Then
        assertThat(period.isShortTerm()).isTrue();
        assertThat(period.isLongTerm()).isFalse();
    }

    @Test
    void shouldBeEqualWhenPeriodsAreSame() {
        // Given
        LocalDateTime start = LocalDateTime.of(2025, 12, 5, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 7, 10, 0);
        RentalPeriod period1 = RentalPeriod.of(start, end);
        RentalPeriod period2 = RentalPeriod.of(start, end);

        // Then
        assertThat(period1).isEqualTo(period2);
        assertThat(period1.hashCode()).isEqualTo(period2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenPeriodsDiffer() {
        // Given
        RentalPeriod period1 = RentalPeriod.of(
            LocalDateTime.of(2025, 12, 5, 10, 0),
            LocalDateTime.of(2025, 12, 7, 10, 0)
        );
        RentalPeriod period2 = RentalPeriod.of(
            LocalDateTime.of(2025, 12, 6, 10, 0),
            LocalDateTime.of(2025, 12, 8, 10, 0)
        );

        // Then
        assertThat(period1).isNotEqualTo(period2);
    }

    @Test
    void shouldReturnCorrectToString() {
        // Given
        LocalDateTime start = LocalDateTime.of(2025, 12, 5, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 7, 10, 0);
        RentalPeriod period = RentalPeriod.of(start, end);

        // When
        String result = period.toString();

        // Then
        assertThat(result).contains("RentalPeriod")
                         .contains("2880 minutes");
    }
}

