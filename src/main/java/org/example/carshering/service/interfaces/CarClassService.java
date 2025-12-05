package org.example.carshering.service.interfaces;

import org.example.carshering.dto.request.create.CreateCarModelName;
import org.example.carshering.dto.response.ModelNameResponse;
import org.example.carshering.domain.entity.CarClass;

import java.util.List;

public interface CarClassService {
    ModelNameResponse createCarClass(CreateCarModelName request);

    CarClass getCarClassByName(String name);

    List<String> findAllClasses();
}
