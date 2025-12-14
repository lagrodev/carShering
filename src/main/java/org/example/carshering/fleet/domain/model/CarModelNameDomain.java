package org.example.carshering.fleet.domain.model;

import lombok.Getter;
import org.example.carshering.fleet.domain.valueobject.id.ModelNameId;
import org.example.carshering.fleet.domain.valueobject.name.ModelName;

@Getter
public class CarModelNameDomain {
    private final ModelNameId id;
    private ModelName name;

    private CarModelNameDomain(ModelNameId id, ModelName name) {
        this.id = id;
        this.name = name;
    }

    public static CarModelNameDomain create(ModelName name) {
        validateRequiredFields(name);
        return new CarModelNameDomain(null, name);
    }

    private static void validateRequiredFields(ModelName name) {
        if (name == null) {
            throw new IllegalArgumentException("Model name cannot be null");
        }
    }

    public static CarModelNameDomain restore(ModelNameId id, ModelName name) {
        if (id == null) {
            throw new IllegalArgumentException("ModelNameId cannot be null when restoring from DB");
        }
        validateRequiredFields(name);
        return new CarModelNameDomain(id, name);
    }

    /**
     * Обновить название модели (например, исправление опечатки)
     * @param newName - новое название
     */
    public void updateName(ModelName newName) {
        if (newName == null) {
            throw new IllegalArgumentException("Model name cannot be null");
        }
        this.name = newName;
        // TODO: Domain Event - ModelNameUpdated
    }
}
