package org.example.carshering.service.interfaces;

import org.example.carshering.dto.request.create.CreateCarModelsBrand;
import org.example.carshering.fleet.api.dto.responce.BrandModelResponse;
import org.example.carshering.fleet.infrastructure.persistence.entity.Brand;

import java.util.List;

public interface CarBrandService {
    BrandModelResponse createBrands(CreateCarModelsBrand request);

    List<String> findAllBrands();

    Brand getBrandByName(String name);
}
