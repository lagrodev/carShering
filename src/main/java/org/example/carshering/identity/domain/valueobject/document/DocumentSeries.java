
package org.example.carshering.identity.domain.valueobject.document;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * Value Object representing document series (passport, driver's license).
 *
 * Valid formats:
 * - Russian passport: 4 digits (e.g., "1234")
 * - Driver's license: 4 digits (e.g., "9876")
 *
 * Immutable - cannot be changed after creation.
 */
@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class DocumentSeries {
    // Document series - usually 4 digits
    private static final Pattern SERIES_PATTERN = Pattern.compile("^\\d{4}$");

    private final String value;

    private DocumentSeries(String value) {
        this.value = value;
    }

    /**
     * Creates DocumentSeries value object from string.
     *
     * @param value document series (4 digits)
     * @return DocumentSeries value object
     * @throws IllegalArgumentException if series is invalid
     */
    public static DocumentSeries of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Document series cannot be null or blank");
        }

        String trimmed = value.trim();

        if (!SERIES_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    String.format("Invalid document series format: '%s'. Expected 4 digits (e.g., '1234')", value)
            );
        }

        return new DocumentSeries(trimmed);
    }

    @Override
    public String toString() {
        return value;
    }
}