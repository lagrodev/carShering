package org.example.carshering.fleet.domain.model;

import lombok.Getter;
import org.example.carshering.common.exceptions.custom.BusinessException;
import org.example.carshering.fleet.domain.valueobject.id.BrandId;
import org.example.carshering.fleet.domain.valueobject.id.CarClassId;
import org.example.carshering.fleet.domain.valueobject.id.ModelId;
import org.example.carshering.fleet.domain.valueobject.id.ModelNameId;
import org.example.carshering.fleet.domain.valueobject.name.BodyType;

@Getter
public class CarModelDomain {
    // final fields
    private final ModelId modelId;

    // other fields
    private BodyType bodyType;
    private CarClassId carClass;
    private ModelNameId model;
    private BrandId brand;
    private boolean deleted;

    private CarModelDomain(
            ModelId modelId,
            BodyType bodyType,
            CarClassId carClass,
            ModelNameId model,
            BrandId brand,
            boolean deleted
    ) {
        this.modelId = modelId;
        this.bodyType = bodyType;
        this.carClass = carClass;
        this.model = model;
        this.brand = brand;
        this.deleted = deleted;
    }

    public static CarModelDomain create(
            BodyType bodyType,
            CarClassId carClass,
            ModelNameId model,
            BrandId brand
    ) {
        validateRequiredFields(
                bodyType,
                carClass,
                model,
                brand
        );
        return new CarModelDomain(
                null,
                bodyType,
                carClass,
                model,
                brand,
                false
        );
    }

    public static CarModelDomain restore(
            ModelId modelId,
            BodyType bodyType,
            CarClassId carClass,
            ModelNameId model,
            BrandId brand,
            boolean deleted
    ) {
        if (modelId == null) {
            throw new BusinessException("ModelId cannot be null for restore operation");
        }

        validateRequiredFields(
                bodyType,
                carClass,
                model,
                brand
        );
        return new CarModelDomain(
                modelId,
                bodyType,
                carClass,
                model,
                brand,
                deleted
        );
    }


    private static void validateRequiredFields(
            BodyType bodyType,
            CarClassId carClass,
            ModelNameId model,
            BrandId brand
    ) {
        if (bodyType == null) {
            throw new BusinessException("BodyType cannot be null");
        }
        if (carClass == null) {
            throw new BusinessException("CarClassId cannot be null");
        }
        if (model == null) {
            throw new BusinessException("ModelNameId cannot be null");
        }
        if (brand == null) {
            throw new BusinessException("BrandId cannot be null");
        }
    }

    /**
     * Update the body type of the car model.
     *
     * @param bodyType - new body type
     */
    public void updateBodyType(BodyType bodyType) {
        if (bodyType == null) {
            throw new BusinessException("BodyType cannot be null");
        }
        this.bodyType = bodyType;
    }

    /**
     * Update the car class of the car model.
     *
     * @param carClass - new car class
     */
    public void updateCarClass(CarClassId carClass) {
        if (carClass == null) {
            throw new BusinessException("CarClassId cannot be null");
        }
        this.carClass = carClass;
    }

    /**
     * Update the model name of the car model.
     *
     * @param model - new model name
     */
    public void updateModel(ModelNameId model) {
        if (model == null) {
            throw new BusinessException("ModelNameId cannot be null");
        }
        this.model = model;
    }


    /**
     * Update the brand of the car model.
     *
     * @param brand - new brand
     */
    public void updateBrand(BrandId brand) {
        if (brand == null) {
            throw new BusinessException("BrandId cannot be null");
        }
        this.brand = brand;
    }

    /**
     * Mark the car model as deleted.
     */
    public void markAsDeleted() {
        this.deleted = true;
    }

}