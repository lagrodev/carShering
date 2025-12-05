package org.example.carshering.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * Value Object representing document number (passport, driver's license).
 *
 * Valid formats:
 * - Russian passport: 6 digits (e.g., "123456")
 * - Driver's license: 6-10 digits (e.g., "1234567890")
 *
 * Immutable - cannot be changed after creation.
 */
@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class DocumentNumber {
    // Document number - 6 to 10 digits
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d{6,10}$");

    private final String value;

    private DocumentNumber(String value) {
        this.value = value;
    }

    /**
     * Creates DocumentNumber value object from string.
     *
     * @param value document number (6-10 digits)
     * @return DocumentNumber value object
     * @throws IllegalArgumentException if number is invalid
     */
    public static DocumentNumber of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Document number cannot be null or blank");
        }

        String trimmed = value.trim();

        if (!NUMBER_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                String.format("Invalid document number format: '%s'. Expected 6-10 digits (e.g., '123456')", value)
            );
        }

        return new DocumentNumber(trimmed);
    }

    /**
     * Returns masked number for security purposes.
     * Shows only last 4 digits.
     * Example: "123456" -> "**3456"
     */
    public String getMasked() {
        if (value.length() <= 4) {
            return value;
        }
        int visibleChars = 4;
        int maskedChars = value.length() - visibleChars;
        return "*".repeat(maskedChars) + value.substring(maskedChars);
    }

    @Override
    public String toString() {
        return value;
    }
}

