package org.example.carshering.service;

import org.example.carshering.dto.request.create.CreateCarModelName;
import org.example.carshering.dto.request.create.CreateCarModelRequest;
import org.example.carshering.dto.request.FilterCarModelRequest;
import org.example.carshering.dto.request.create.CreateCarModelsBrand;
import org.example.carshering.dto.request.update.UpdateCarModelRequest;
import org.example.carshering.dto.response.BrandModelResponse;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.dto.response.ModelNameResponse;
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

    BrandModelResponse createBrands(CreateCarModelsBrand updateCarModelsBrand);

    ModelNameResponse createModelName(CreateCarModelName updateCarModelsModel);
    ModelNameResponse createCarClass(CreateCarModelName updateCarModelsModel);
}
