package org.example.carshering.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;

/**
 * Value Object представляющий дату выдачи документа.
 *
 * Правила валидации:
 * - Дата не может быть в будущем
 * - Дата не может быть слишком старой (более 100 лет назад)
 *
 * Immutable - не может быть изменен после создания.
 */
@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class DateOfIssue {
    private static final int MAX_YEARS_AGO = 100;

    private final LocalDate value;

    private DateOfIssue(LocalDate value) {
        this.value = value;
    }

    /**
     * Создает DateOfIssue value object.
     *
     * @param value дата выдачи документа
     * @return DateOfIssue value object
     * @throws IllegalArgumentException если дата невалидна
     */
    public static DateOfIssue of(LocalDate value) {
        if (value == null) {
            throw new IllegalArgumentException("Дата выдачи документа не может быть null");
        }

        LocalDate today = LocalDate.now();

        if (value.isAfter(today)) {
            throw new IllegalArgumentException(
                String.format("Дата выдачи документа не может быть в будущем: %s", value)
            );
        }

        LocalDate minDate = today.minusYears(MAX_YEARS_AGO);
        if (value.isBefore(minDate)) {
            throw new IllegalArgumentException(
                String.format("Дата выдачи документа слишком старая (более %d лет назад): %s", MAX_YEARS_AGO, value)
            );
        }

        return new DateOfIssue(value);
    }

    /**
     * Возвращает количество лет с момента выдачи документа.
     */
    public int getYearsSinceIssue() {
        return Period.between(value, LocalDate.now()).getYears();
    }

    /**
     * Проверяет, является ли документ недавно выданным (менее года назад).
     */
    public boolean isRecent() {
        return getYearsSinceIssue() < 1;
    }

    /**
     * Проверяет, является ли документ старым (более 10 лет).
     */
    public boolean isOld() {
        return getYearsSinceIssue() > 10;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

