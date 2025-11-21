package org.example.carshering.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.create.CreateCarModelName;
import org.example.carshering.dto.response.ModelNameResponse;
import org.example.carshering.entity.Model;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.mapper.ModelNameMapper;
import org.example.carshering.repository.ModelNameRepository;
import org.example.carshering.service.interfaces.CarModelNameService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarModelNameServiceImpl implements CarModelNameService {

    private final ModelNameMapper modelNameMapper;
    private final ModelNameRepository modelNameRepository;

    @Override
    public List<String> findAllModels() {
        return modelNameRepository.findAll()
                .stream()
                .map(Model::getName)
                .toList();
    }

    @Override
    public ModelNameResponse createModelName(CreateCarModelName request) {
        Model saved = modelNameRepository.save(modelNameMapper.toEntity(request));

        return modelNameMapper.toDto(saved);
    }

    @Override
    public Model getModelByName(String name) {
        return modelNameRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NotFoundException("Model not found"));
    }

}
