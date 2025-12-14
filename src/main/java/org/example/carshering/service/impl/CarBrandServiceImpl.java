//package org.example.carshering.service.impl;
//
//import lombok.RequiredArgsConstructor;
//import org.example.carshering.dto.request.create.CreateCarModelsBrand;
//import org.example.carshering.fleet.api.dto.responce.BrandModelResponse;
//import org.example.carshering.fleet.infrastructure.persistence.entity.Brand;
//import org.example.carshering.common.exceptions.custom.NotFoundException;
//import org.example.carshering.mapper.BrandMapper;
//import org.example.carshering.fleet.infrastructure.persistence.repository.BrandRepository;
//import org.example.carshering.service.interfaces.CarBrandService;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class CarBrandServiceImpl implements CarBrandService {
//
//    private final BrandMapper brandMapper;
//    private final BrandRepository carBrandRepository;
//
//    @Override
//    public BrandModelResponse createBrands(CreateCarModelsBrand request) {
//
//        Brand saved = brandMapper.toEntity(request);
//        System.out.println(saved);
//        System.out.println("--------");
//        saved = carBrandRepository.save(saved);
//        System.out.println(saved);
//        return brandMapper.toDto(saved);
//
//    }
//
//
//    @Override
//    public List<String> findAllBrands() {
//        return carBrandRepository.findAll().stream()
//                .map(Brand::getName)
//                .toList();
//    }
//
//    @Override
//    public Brand getBrandByName(String name) {
//        return carBrandRepository.findByNameIgnoreCase(name)
//                .orElseThrow(() -> new NotFoundException("Brand not found"));
//    }
//}
