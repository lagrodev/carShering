package org.example.carshering.domain.valueobject;


import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * Value Object VIN (Vehicle Identification Number).
 * Validation rules:
 * - Exactly 17 characters
 * - Only letters A-Z (except I, O, Q) and numbers 0-9
 * - Converted to uppercase
 *
 * Immutable - cannot be changed after creation.
 */
@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class Vin {
    // VIN cannot contain the letters I, O, Q (confusion with 1, 0)
    private static final Pattern VIN_PATTERN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

    private final String value;

    private Vin(String value) {
        this.value = value;
    }

    /**
     * Creates a VIN value object from a string.
     *
     * @param value VIN number (17 characters)
     * @return VIN value object
     * @throws IllegalArgumentException if the VIN is invalid
     */
    public static Vin of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("VIN cannot be null or blank");
        }

        String trimmed = value.trim().toUpperCase();

        if (trimmed.length() != 17) {
            throw new IllegalArgumentException(
                String.format("VIN must contain exactly 17 characters, received: %d", trimmed.length())
            );
        }

        if (!VIN_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                "The VIN contains invalid characters. Allowed characters are: A-Z (except I, O, Q) and 0-9"
            );
        }

        return new Vin(trimmed);
    }

    /**
     * Returns the World Manufacturer's Identifier (WMI) - the first 3 characters of the VIN.
     */
    public String getWmi() {
        return value.substring(0, 3);
    }

    /**
     * Returns the descriptive portion of the VIN (VDS) - characters 4-9.
     */
    public String getVds() {
        return value.substring(3, 9);
    }

    /**
     * Returns the VIN's distinguishing part (VIS) - characters 10-17.
     */
    public String getVis() {
        return value.substring(9, 17);
    }

    @Override
    public String toString() {
        return value;
    }
}
