package org.example.carshering.service;

import org.example.carshering.dto.request.CreateCarModelRequest;
import org.example.carshering.dto.request.FilterCarModelRequest;
import org.example.carshering.dto.request.UpdateCarModelRequest;
import org.example.carshering.dto.response.CarModelResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CarModelService {

    void deleteModel(Long modelId);
    CarModelResponse createModel(CreateCarModelRequest request);




    Page<CarModelResponse> findActiveModels(FilterCarModelRequest request,
                                            Pageable pageable);

    Page<CarModelResponse> getAllModelsIncludingDeleted(FilterCarModelRequest request,
                                                        Pageable pageable);
    CarModelResponse getModelById(Long modelId);

    CarModelResponse updateModel(Long modelId, UpdateCarModelRequest request);

    List<String> findAllClasses();

    List<String> findAllBodyTypes();

    List<String> findAllModels();

    List<String> findAllBrands();
}
