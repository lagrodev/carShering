package org.example.carshering.fleet.application.service;

import org.example.carshering.dto.request.FilterCarModelRequest;
import org.example.carshering.dto.request.create.CreateCarModelRequest;
import org.example.carshering.dto.request.update.UpdateCarModelRequest;
import org.example.carshering.fleet.application.dto.response.CarModelDto;
import org.example.carshering.fleet.domain.valueobject.id.ModelId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CarModelApplicationService {

    @Transactional
    void deleteModel(ModelId modelId);

    @Transactional
    CarModelDto createModel(CreateCarModelRequest request);

    Page<CarModelDto> getAllModelsIncludingDeleted(FilterCarModelRequest request,
                                                        Pageable pageable);

    CarModelDto getModelById(ModelId modelId);

    CarModelDto updateModel(ModelId modelId, UpdateCarModelRequest request);

    List<String> findAllBodyTypes();

}
