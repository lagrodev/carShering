package org.example.carshering.service;

import org.example.carshering.dto.request.CreateCarModelRequest;
import org.example.carshering.dto.response.CarModelResponse;

import java.util.List;

public interface CarModelService {

    void deleteModel(Long modelId);
    CarModelResponse createModel(CreateCarModelRequest request);


    List<CarModelResponse> getModels(String brand, String bodyType, String carClass);
    List<CarModelResponse> getAllModelsIncludingDeleted(String brand, String bodyType, String carClass);
    CarModelResponse getModelById(Long modelId);
}
