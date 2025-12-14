package org.example.carshering.fleet.domain.valueobject;


import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * A value object representing a state registration number (GRZ).
 * Supports Russian number formats.
 *
 * Examples of valid formats:
 * - A123BC77 (standard format)
 * - A123BC777 (for some regions)
 * - AB12377 (taxi, public transport)
 *
 * Converted to uppercase.
 * Immutable - cannot be changed after creation.
 */
@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class GosNumber {
    // Паттерн для российских номеров: буква + 3 цифры + 2 буквы + регион (2-3 цифры)
    // Допустимые буквы: А, В, Е, К, М, Н, О, Р, С, Т, У, Х (совпадают с латиницей)
    private static final Pattern GOS_NUMBER_PATTERN = Pattern.compile(
        "^[АВЕКМНОРСТУХ]\\d{3}[АВЕКМНОРСТУХ]{2}\\d{2,3}$"
    );

    private final String value;

    private GosNumber(String value) {
        this.value = value;
    }

    /**
     * Creates a GosNumber value object from a string.
     * Automatically removes spaces and hyphens.
     *
     * @param value state registration number (for example: "A123BC77" or "A 123 BC 77")
     * @return GosNumber value object
     * @throws IllegalArgumentException if the number is invalid
     */
    public static GosNumber of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("GosNumber cannot be null or blank");
        }

        // Удаляем пробелы и дефисы, приводим к верхнему регистру
        String normalized = value.trim()
                                 .toUpperCase()
                                 .replaceAll("[\\s-]", "");

        // Базовая проверка длины (минимум 8, максимум 9 символов)
        if (normalized.length() < 8 || normalized.length() > 9) {
            throw new IllegalArgumentException(
                String.format("Invalid license plate length: %d characters. Expected to be 8-9.", normalized.length())
            );
        }

        // Проверка формата
        if (!GOS_NUMBER_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Invalid license plate format. Expected format: A123BC77" +
                            "(letter + 3 digits + 2 letters + 2-3 region digits)." +
                            "Acceptable letters: А, В, Е, К, М, Н, О, Р, С, Т, У, Х"
            );
        }

        return new GosNumber(normalized);
    }

    /**
     * Returns the region number (last 2-3 digits).
     */
    public String getRegion() {
        // Регион - это последние 2 или 3 цифры
        int length = value.length();
        int regionStart = length == 8 ? 6 : 5;
        return value.substring(regionStart);
    }

    /**
     * Returns a formatted number for display.
     * Example: A123BC77 -> A 123 BC 77
     */
    public String getFormatted() {
        if (value.length() == 8) {
            // А123ВС77 -> А 123 ВС 77
            return String.format("%s %s %s %s",
                value.substring(0, 1),
                value.substring(1, 4),
                value.substring(4, 6),
                value.substring(6, 8)
            );
        } else {
            // А123ВС777 -> А 123 ВС 777
            return String.format("%s %s %s %s",
                value.substring(0, 1),
                value.substring(1, 4),
                value.substring(4, 6),
                value.substring(6, 9)
            );
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
