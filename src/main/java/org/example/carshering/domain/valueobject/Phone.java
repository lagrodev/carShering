package org.example.carshering.domain.valueobject;


import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing a phone number.
 * Supports Russian phone formats: +7XXXXXXXXXX or 8XXXXXXXXXX
 * Immutable - cannot be changed after creation.
 */
@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class Phone {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+7|8)\\d{10}$");
    private static final Pattern DIGITS_ONLY = Pattern.compile("\\D");

    private final String value;

    private Phone(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Phone cannot be null or blank");
        }

        String normalized = normalize(value);

        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                "Invalid phone format. Expected: +7XXXXXXXXXX or 8XXXXXXXXXX, got: " + value
            );
        }

        this.value = normalized;
    }

    /**
     * Creates a Phone value object from a string.
     * Normalizes the input by removing spaces, dashes, and parentheses.
     *
     * @param value raw phone number string
     * @return Phone value object
     * @throws IllegalArgumentException if phone format is invalid
     */
    public static Phone of(String value) {
        Objects.requireNonNull(value, "Phone value cannot be null");
        return new Phone(value.trim());
    }

    /**
     * Normalizes phone number by removing all non-digit characters except leading +.
     * Examples:
     *   "+7 (999) 123-45-67" -> "+79991234567"
     *   "8 999 123 45 67" -> "89991234567"
     *   "+7-999-123-45-67" -> "+79991234567"
     */
    private String normalize(String phone) {
        String result = phone.trim();

        // Keep leading + if present
        boolean hasPlus = result.startsWith("+");

        // Remove all non-digit characters
        result = DIGITS_ONLY.matcher(result).replaceAll("");

        // Restore + if it was present
        if (hasPlus) {
            result = "+" + result;
        }

        return result;
    }

    /**
     * Returns formatted phone number for display.
     * Example: +79991234567 -> +7 (999) 123-45-67
     */
    public String getFormatted() {
        if (value.startsWith("+7") && value.length() == 12) {
            return String.format("+7 (%s) %s-%s-%s",
                value.substring(2, 5),
                value.substring(5, 8),
                value.substring(8, 10),
                value.substring(10, 12)
            );
        } else if (value.startsWith("8") && value.length() == 11) {
            return String.format("8 (%s) %s-%s-%s",
                value.substring(1, 4),
                value.substring(4, 7),
                value.substring(7, 9),
                value.substring(9, 11)
            );
        }
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
