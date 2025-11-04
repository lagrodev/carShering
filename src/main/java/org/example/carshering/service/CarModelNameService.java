package org.example.carshering.service;

import org.example.carshering.dto.request.create.CreateCarModelName;
import org.example.carshering.dto.request.create.CreateCarModelsBrand;
import org.example.carshering.dto.response.BrandModelResponse;
import org.example.carshering.dto.response.ModelNameResponse;
import org.example.carshering.entity.Model;

import java.util.List;

public interface CarModelNameService {
    ModelNameResponse createModelName(CreateCarModelName request);

    List<String> findAllModels();

    Model getModelByName(String name);
}
