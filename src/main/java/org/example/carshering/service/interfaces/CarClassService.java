package org.example.carshering.service.interfaces;

import org.example.carshering.dto.request.create.CreateCarModelName;
import org.example.carshering.fleet.api.dto.responce.ModelNameResponse;
import org.example.carshering.fleet.infrastructure.persistence.entity.CarClass;

import java.util.List;

public interface CarClassService {
    ModelNameResponse createCarClass(CreateCarModelName request);

    CarClass getCarClassByName(String name);

    List<String> findAllClasses();
}
