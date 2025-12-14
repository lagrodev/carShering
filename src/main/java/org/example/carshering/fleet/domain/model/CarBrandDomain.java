package org.example.carshering.fleet.domain.model;

import lombok.Getter;
import org.example.carshering.fleet.domain.valueobject.id.BrandId;
import org.example.carshering.fleet.domain.valueobject.name.BrandName;

@Getter
public class CarBrandDomain {
    private final BrandId id;
    private BrandName name;

    private CarBrandDomain(BrandId id, BrandName name) {
        this.id = id;
        this.name = name;
    }

    public static CarBrandDomain create(BrandName name) {
        validateRequiredFields(name);
        return new CarBrandDomain(null, name);
    }

    private static void validateRequiredFields(BrandName name) {
        if (name == null) {
            throw new IllegalArgumentException("Brand name cannot be null");
        }
    }

    public static CarBrandDomain restore(BrandId id, BrandName name) {
        if (id == null) {
            throw new IllegalArgumentException("BrandId cannot be null when restoring from DB");
        }
        validateRequiredFields(name);
        return new CarBrandDomain(id, name);
    }

    /**
     * Обновить название бренда (например, исправление опечатки)
     * @param newName - новое название
     */
    public void updateName(BrandName newName) {
        if (newName == null) {
            throw new IllegalArgumentException("Brand name cannot be null");
        }
        this.name = newName;
        // TODO: Domain Event - BrandNameUpdated
    }
}
