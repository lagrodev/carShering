package org.example.carshering.fleet.domain.model;

import lombok.Getter;
import org.example.carshering.fleet.domain.valueobject.id.CarClassId;
import org.example.carshering.fleet.domain.valueobject.name.CarClassName;

@Getter
public class CarClassDomain {
    private final CarClassId id;
    private CarClassName name;

    private CarClassDomain(CarClassId id, CarClassName name) {
        this.id = id;
        this.name = name;
    }

    public static CarClassDomain create(CarClassName name) {
        validateRequiredFields(name);
        return new CarClassDomain(null, name);
    }

    private static void validateRequiredFields(CarClassName name) {
        if (name == null) {
            throw new IllegalArgumentException("CarClass name cannot be null");
        }
    }

    public static CarClassDomain restore(CarClassId id, CarClassName name) {
        if (id == null) {
            throw new IllegalArgumentException("CarClassId cannot be null when restoring from DB");
        }
        validateRequiredFields(name);
        return new CarClassDomain(id, name);
    }

    /**
     * Обновить название класса автомобиля (например, исправление опечатки)
     * @param newName - новое название
     */
    public void updateName(CarClassName newName) {
        if (newName == null) {
            throw new IllegalArgumentException("CarClass name cannot be null");
        }
        this.name = newName;
        // TODO: Domain Event - CarClassNameUpdated
    }
}
