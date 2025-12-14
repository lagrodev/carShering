package org.example.carshering.fleet.api.facade;

import lombok.RequiredArgsConstructor;
import org.example.carshering.fleet.api.dto.responce.CarModelResponse;
import org.example.carshering.fleet.api.mapper.CarModelApiMapper;
import org.example.carshering.fleet.application.dto.response.CarModelDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Фасад для сборки CarModelResponse
 * Для CarModel дополнительных данных не требуется, просто делегирует в маппер
 */
@Component
@RequiredArgsConstructor
public class CarModelFacade {

    private final CarModelApiMapper carModelApiMapper;

    public CarModelResponse toResponse(CarModelDto dto) {
        return carModelApiMapper.toResponse(dto);
    }

    public List<CarModelResponse> toResponseList(List<CarModelDto> dtos) {
        return carModelApiMapper.toResponseList(dtos);
    }

    public Page<CarModelResponse> toResponsePage(Page<CarModelDto> dtoPage) {
        return carModelApiMapper.toResponsePage(dtoPage);
    }
}

