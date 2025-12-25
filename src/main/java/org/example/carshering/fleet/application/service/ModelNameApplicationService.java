package org.example.carshering.fleet.application.service;

import org.example.carshering.fleet.api.dto.request.create.CreateCarModelName;
import org.example.carshering.fleet.application.dto.response.ModelNameDto;

import java.util.List;

public interface ModelNameApplicationService {
    ModelNameDto createCarClass(CreateCarModelName request);

    List<ModelNameDto> getAllCarClass();
}
