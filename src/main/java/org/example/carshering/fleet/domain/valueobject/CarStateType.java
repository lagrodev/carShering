package org.example.carshering.fleet.domain.valueobject;

import lombok.Getter;

@Getter
public enum CarStateType {
    AVAILABLE("Доступен"),
    CONFIRMED("Подтвержден"),
    ACTIVE("Выполняется"),
    CANCELLED("Отменен"),
    CLOSED("Завершен");

    private final String description;

    CarStateType(String description) {
        this.description = description;
    }
}
