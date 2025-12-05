package org.example.carshering.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Value Object representing the authority that issued the document.
 *
 * Примеры:
 * - "ФМС России"
 * - "МВД России"
 * - "УМВД России по Московской области"
 * - "ГИБДД МВД России"
 *
 * Immutable - cannot be changed after creation.
 */
@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class IssuingAuthority {
    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 500;

    private final String value;

    private IssuingAuthority(String value) {
        this.value = value;
    }

    /**
     * Creates IssuingAuthority value object from string.
     *
     * @param value issuing authority name
     * @return IssuingAuthority value object
     * @throws IllegalArgumentException if name is invalid
     */
    public static IssuingAuthority of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Issuing authority cannot be null or blank");
        }

        String trimmed = value.trim();

        if (trimmed.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Issuing authority name is too short (minimum %d characters): '%s'", MIN_LENGTH, value)
            );
        }

        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Issuing authority name is too long (maximum %d characters): length %d", MAX_LENGTH, trimmed.length())
            );
        }

        return new IssuingAuthority(trimmed);
    }

    /**
     * Returns shortened name (first 50 characters).
     */
    public String getShortName() {
        if (value.length() <= 50) {
            return value;
        }
        return value.substring(0, 47) + "...";
    }

    @Override
    public String toString() {
        return value;
    }
}

