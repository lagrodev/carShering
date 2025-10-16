package org.example.carshering.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.CreateCarModelRequest;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.entity.CarModel;
import org.example.carshering.mapper.ModelMapper;
import org.example.carshering.repository.CarModelRepository;
import org.example.carshering.service.CarModelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarModelServiceImpl implements CarModelService {

    private CarModelRepository carModelRepository;
    private final ModelMapper modelMapper;

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

    @Override
    public List<CarModelResponse> getModels(String brand, String bodyType, String carClass) {

        return carModelRepository.findModelsByFilter(brand, bodyType, carClass)
                .stream()
                .map(modelMapper::toDto)
                .toList();
    }

    @Override
    public List<CarModelResponse> getAllModelsIncludingDeleted(String brand, String bodyType, String carClass) {
        return carModelRepository.findModelsByFilterIncludingDeleted(brand, bodyType, carClass)
                .stream()
                .map(modelMapper::toDto)
                .toList();
    }

    @Override
    public CarModelResponse getModelById(Long modelId) {
        CarModel model = carModelRepository.findByIdAndDeletedFalse(modelId)
                .orElseThrow(() -> new EntityNotFoundException("Model not found"));
        return modelMapper.toDto(model);
    }

}
