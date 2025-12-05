package org.example.carshering.domain.valueobject;


import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Value Object representing the year the vehicle was manufactured.
 *
 * Validation rules:
 * - Minimum year: 1886 (first vehicle)
 * - Maximum year: current year + 1 (for future models)
 *
 * Immutable - cannot be changed after creation.
 */
@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class Year {
    private static final int MIN_YEAR = 1886;

    private final int value;

    private Year(int value) {
        this.value = value;
    }

    /**
     * Creates a Year value object.
     *
     * @param value year of manufacture (1886 - current year + 1)
     * @return Year value object
     * @throws IllegalArgumentException if the year is out of range
     */
    public static Year of(int value) {
        int currentYear = LocalDate.now().getYear();
        int maxYear = currentYear + 1; // Допускаем модели следующего года

        if (value < MIN_YEAR) {
            throw new IllegalArgumentException(
                String.format("The year of manufacture cannot be less than %d (first car)", MIN_YEAR)
            );
        }

        if (value > maxYear) {
            throw new IllegalArgumentException(
                String.format("The year of issue cannot be greater than %d (current year + 1)", maxYear)
            );
        }

        return new Year(value);
    }

    /**
     * Returns the vehicle's age in years.
     */
    public int getAge() {
        int currentYear = LocalDate.now().getYear();
        return currentYear - value;
    }

    /**
     * Checks whether the vehicle is new (less than 1 year old).
     */
    public boolean isNew() {
        return getAge() <= 1;
    }

    /**
     * Checks whether the vehicle is old (more than 10 years old).
     */
    public boolean isOld() {
        return getAge() > 10;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
