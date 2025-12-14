package org.example.carshering.service.interfaces;

import org.example.carshering.dto.request.create.CreateCarModelName;
import org.example.carshering.fleet.api.dto.responce.ModelNameResponse;
import org.example.carshering.fleet.infrastructure.persistence.entity.Model;

import java.util.List;

public interface CarModelNameService {
    ModelNameResponse createModelName(CreateCarModelName request);

    List<String> findAllModels();

    Model getModelByName(String name);
}
