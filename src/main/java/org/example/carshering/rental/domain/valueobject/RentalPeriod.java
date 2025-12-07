package org.example.carshering.rental.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Value Object representing a rental period.
 * Encapsulates start date, end date and duration in minutes.
 *
 * Validation rules:
 * - Start date must be before end date
 * - Period must not be too long (maximum 90 days)
 * - Minimum duration - 1 hour
 *
 * Immutable - cannot be changed after creation.
 */
@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class RentalPeriod {
    private static final long MAX_RENTAL_DAYS = 90;
    private static final long MIN_RENTAL_MINUTES = 30; // 1 hour

    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Long durationMinutes;

    private RentalPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.durationMinutes = Duration.between(startDate, endDate).toMinutes();
    }

    /**
     * Creates RentalPeriod value object.
     *
     * @param startDate rental start date and time
     * @param endDate rental end date and time
     * @return RentalPeriod value object
     * @throws IllegalArgumentException if period is invalid
     */
    public static RentalPeriod of(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Rental start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("Rental end date cannot be null");
        }

        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException(
                String.format("Rental end date (%s) must be after start date (%s)",
                    endDate, startDate)
            );
        }

        long durationMinutes = Duration.between(startDate, endDate).toMinutes();

        if (durationMinutes < MIN_RENTAL_MINUTES) {
            throw new IllegalArgumentException(
                String.format("Minimum rental duration: %d minutes (1 hour). Got: %d minutes",
                    MIN_RENTAL_MINUTES, durationMinutes)
            );
        }

        long durationDays = Duration.between(startDate, endDate).toDays();
        if (durationDays > MAX_RENTAL_DAYS) {
            throw new IllegalArgumentException(
                String.format("Maximum rental duration: %d days. Got: %d days",
                    MAX_RENTAL_DAYS, durationDays)
            );
        }

        return new RentalPeriod(startDate, endDate);
    }

    /**
     * Returns rental duration in hours.
     */
    public long getDurationInHours() {
        return durationMinutes / 60;
    }

    /**
     * Returns rental duration in days.
     */
    public long getDurationInDays() {
        return durationMinutes / (60 * 24);
    }

    /**
     * Checks if this period overlaps with another period.
     *
     * @param other another rental period
     * @return true if periods overlap
     */
    public boolean overlaps(RentalPeriod other) {
        if (other == null) {
            return false;
        }
        return !this.endDate.isBefore(other.startDate) && !this.startDate.isAfter(other.endDate);
    }

    /**
     * Checks if period contains specified date.
     *
     * @param date date to check
     * @return true if date is within the period
     */
    public boolean contains(LocalDateTime date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Checks if rental is long-term (more than 7 days).
     */
    public boolean isLongTerm() {
        return getDurationInDays() > 7;
    }

    /**
     * Checks if rental is short-term (less than 24 hours).
     */
    public boolean isShortTerm() {
        return getDurationInHours() < 24;
    }

    /**
     * Checks if rental has already started.
     */
    public boolean hasStarted() {
        return LocalDateTime.now().isAfter(startDate);
    }

    /**
     * Checks if rental has already ended.
     */
    public boolean hasEnded() {
        return LocalDateTime.now().isAfter(endDate);
    }

    /**
     * Checks if rental is currently active.
     */
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    @Override
    public String toString() {
        return String.format("RentalPeriod[%s - %s, %d minutes]",
            startDate, endDate, durationMinutes);
    }

    public void updatePeriod(RentalPeriod newPeriod) {
    }
}
