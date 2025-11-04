package org.example.carshering.service;

import org.example.carshering.dto.request.create.CreateCarModelName;
import org.example.carshering.dto.request.create.CreateCarModelsBrand;
import org.example.carshering.dto.response.BrandModelResponse;
import org.example.carshering.dto.response.ModelNameResponse;
import org.example.carshering.entity.CarClass;

import java.util.List;

public interface CarClassService {
    ModelNameResponse createCarClass(CreateCarModelName request);

    CarClass getCarClassByName(String name);

    List<String> findAllClasses();
}
