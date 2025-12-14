package org.example.carshering.fleet.api.facade;

import lombok.RequiredArgsConstructor;
import org.example.carshering.fleet.api.dto.responce.CarClassResponse;
import org.example.carshering.fleet.api.dto.responce.ModelNameResponse;
import org.example.carshering.fleet.api.mapper.CarClassApiMapper;
import org.example.carshering.fleet.application.dto.response.CarClassDto;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Фасад для сборки CarClassResponse
 * Для CarClass дополнительных данных не требуется, просто делегирует в маппер
 */
@Component
@RequiredArgsConstructor
public class CarClassFacade {

    private final CarClassApiMapper carClassApiMapper;

    public ModelNameResponse toResponse(CarClassDto dto) {
        return carClassApiMapper.toResponse(dto);
    }

    public List<ModelNameResponse> toResponseList(List<CarClassDto> dtos) {
        return carClassApiMapper.toResponseList(dtos);
    }
}

