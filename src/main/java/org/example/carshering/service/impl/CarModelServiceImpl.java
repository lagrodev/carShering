package org.example.carshering.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.CreateCarModelRequest;
import org.example.carshering.dto.request.FilterCarModelRequest;
import org.example.carshering.dto.request.UpdateCarModelRequest;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.entity.CarModel;
import org.example.carshering.mapper.ModelMapper;
import org.example.carshering.repository.CarModelRepository;
import org.example.carshering.service.CarModelService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CarModelServiceImpl implements CarModelService {

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "brand", "model", "bodyType", "carClass"
    );
    private final ModelMapper modelMapper;
    private final CarModelRepository carModelRepository;

    @Override
    @Transactional
    public void deleteModel(Long modelId) {
        CarModel model = carModelRepository.findByIdAndDeletedFalse(modelId)
                .orElseThrow(() -> new RuntimeException("Модель не найдена"));

        model.setDeleted(true);
        carModelRepository.save(model);
    }

    @Override
    public CarModelResponse createModel(CreateCarModelRequest request) {
        CarModel saved = carModelRepository.save(modelMapper.toEntity(request));
        return modelMapper.toDto(saved);
    }

    private void validateSortProperties(Sort sort) {
        for (Sort.Order order : sort) {
            if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
                throw new IllegalArgumentException("Недопустимое поле сортировки: " + order.getProperty());
            }
        }
    }

    @Override
    public Page<CarModelResponse> findActiveModels(FilterCarModelRequest request,
                                                   Pageable pageable) {
        validateSortProperties(pageable.getSort());
        return carModelRepository.findModelsByFilter(
                false,
                        request.brand(), request.bodyType(), request.carClass(), pageable)
                .map(modelMapper::toDto)
                ;
    }

    @Override
    public Page<CarModelResponse> getAllModelsIncludingDeleted(FilterCarModelRequest request,
                                                               Pageable pageable
    ) {
        return carModelRepository.findModelsByFilter(true,request.brand(), request.bodyType(), request.carClass(), pageable)
                .map(modelMapper::toDto);
    }

    @Override
    public CarModelResponse getModelById(Long modelId) {
        CarModel model = carModelRepository.findByIdAndDeletedFalse(modelId)
                .orElseThrow(() -> new EntityNotFoundException("Model not found"));
        return modelMapper.toDto(model);
    }

    @Override
    public CarModelResponse updateModel(Long modelId, UpdateCarModelRequest request) {
        CarModel model = carModelRepository.findByIdAndDeletedFalse(modelId)
                .orElseThrow(() -> new EntityNotFoundException("Model not found"));


        modelMapper.updateCarFromDto(request, model);

        return modelMapper.toDto(carModelRepository.save(model));
    }

}
