package org.example.carshering.fleet.api.facade;

import lombok.RequiredArgsConstructor;
import org.example.carshering.fleet.api.dto.responce.ModelNameResponse;
import org.example.carshering.fleet.api.mapper.ModelNameApiMapper;
import org.example.carshering.fleet.application.dto.response.ModelNameDto;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Фасад для сборки ModelNameResponse
 * Для ModelName дополнительных данных не требуется, просто делегирует в маппер
 */
@Component
@RequiredArgsConstructor
public class ModelNameFacade {

    private final ModelNameApiMapper modelNameApiMapper;

    public ModelNameResponse toResponse(ModelNameDto dto) {
        return modelNameApiMapper.toResponse(dto);
    }

    public List<ModelNameResponse> toResponseList(List<ModelNameDto> dtos) {
        return modelNameApiMapper.toResponseList(dtos);
    }
}

