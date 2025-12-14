package org.example.carshering.fleet.application.service;

import org.example.carshering.dto.request.create.CreateCarModelsBrand;
import org.example.carshering.fleet.api.dto.request.CreateBrandRequest;
import org.example.carshering.fleet.application.dto.response.BrandDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BrandApplicationService {
    BrandDto createBrand(CreateCarModelsBrand request);

    @Transactional(readOnly = true)
    public List<BrandDto> getAllBrands();
}
