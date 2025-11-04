package org.example.carshering.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.FilterCarModelRequest;
import org.example.carshering.dto.request.create.CreateCarModelRequest;
import org.example.carshering.dto.request.update.UpdateCarModelRequest;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.entity.Car;
import org.example.carshering.entity.CarModel;
import org.example.carshering.exceptions.custom.EntityNotFoundException;
import org.example.carshering.exceptions.custom.InvalidQueryParameterException;
import org.example.carshering.mapper.ModelMapper;
import org.example.carshering.repository.CarModelRepository;
import org.example.carshering.service.CarModelService;
import org.example.carshering.service.CarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CarModelServiceImpl implements CarModelService {

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "brand", "model", "bodyType", "carClass"
    );
    private final ModelMapper modelMapper;
    private final CarModelRepository carModelRepository;
    private final CarService carService;

    @Override
    @Transactional
    public void deleteModel(Long modelId) {
        CarModel model = carModelRepository.findByIdAndDeletedFalse(modelId)
                .orElseThrow(() -> new EntityNotFoundException("Model not found"));

        for (Car car : model.getCars()) {
            carService.deleteCar(car.getId());
        }

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
                throw new InvalidQueryParameterException(order.getProperty());
            }
        }
    }


    @Override
    public Page<CarModelResponse> getAllModelsIncludingDeleted(FilterCarModelRequest request,
                                                               Pageable pageable
    ) {
        validateSortProperties(pageable.getSort());
        return carModelRepository.findModelsByFilter(request.deleted(), request.brand(), request.bodyType(), request.carClass(), pageable)
                .map(modelMapper::toDto);
    }

    @Override
    public CarModelResponse getModelById(Long modelId) {
        CarModel model = carModelRepository.findByIdAndDeletedFalse(modelId)
                .orElseThrow(() -> new EntityNotFoundException("Model not found"));
        return modelMapper.toDto(model);
    }

    // todo я более чем уверен, что он не правильно обновляет, проверить и исправить
    @Override
    public CarModelResponse updateModel(Long modelId, UpdateCarModelRequest request) {

        CarModel model = carModelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("Model not found"));


        modelMapper.updateCarFromDto(request, model);

        return modelMapper.toDto(carModelRepository.save(model));
    }

    @Override
    public List<String> findAllBodyTypes() {
        return carModelRepository.findDistinctBodyTypes();
    }

    @Override
    public CarModel getCarModelById(Long modelId) {
        return carModelRepository.findByIdAndDeletedFalse(modelId)
                .orElseThrow(() -> new EntityNotFoundException("Model not found"));
    }


}
