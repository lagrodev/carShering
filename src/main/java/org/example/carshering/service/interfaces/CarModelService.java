package org.example.carshering.service.interfaces;

import org.example.carshering.dto.request.FilterCarModelRequest;
import org.example.carshering.dto.request.create.CreateCarModelRequest;
import org.example.carshering.dto.request.update.UpdateCarModelRequest;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.entity.CarModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CarModelService {

    void deleteModel(Long modelId);

    CarModelResponse createModel(CreateCarModelRequest request);

    Page<CarModelResponse> getAllModelsIncludingDeleted(FilterCarModelRequest request,
                                                        Pageable pageable);

    CarModelResponse getModelById(Long modelId);

    CarModelResponse updateModel(Long modelId, UpdateCarModelRequest request);

    CarModel getCarModelById(Long modelId);

    List<String> findAllBodyTypes();

}
