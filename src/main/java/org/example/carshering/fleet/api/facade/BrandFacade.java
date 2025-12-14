package org.example.carshering.fleet.api.facade;

import lombok.RequiredArgsConstructor;
import org.example.carshering.fleet.api.dto.responce.BrandResponse;
import org.example.carshering.fleet.api.mapper.BrandApiMapper;
import org.example.carshering.fleet.application.dto.response.BrandDto;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Фасад для сборки BrandResponse
 * Для Brand дополнительных данных не требуется, просто делегирует в маппер
 */
@Component
@RequiredArgsConstructor
public class BrandFacade {

    private final BrandApiMapper brandApiMapper;

    public BrandResponse toResponse(BrandDto dto) {
        return brandApiMapper.toResponse(dto);
    }

    public List<BrandResponse> toResponseList(List<BrandDto> dtos) {
        return brandApiMapper.toResponseList(dtos);
    }
}

