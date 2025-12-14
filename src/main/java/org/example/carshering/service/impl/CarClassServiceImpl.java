//package org.example.carshering.service.impl;
//
//import lombok.RequiredArgsConstructor;
//import org.example.carshering.dto.request.create.CreateCarModelName;
//import org.example.carshering.fleet.api.dto.responce.ModelNameResponse;
//import org.example.carshering.fleet.infrastructure.persistence.entity.CarClass;
//import org.example.carshering.common.exceptions.custom.NotFoundException;
//import org.example.carshering.mapper.CarClassMapper;
//import org.example.carshering.fleet.infrastructure.persistence.repository.CarClassRepository;
//import org.example.carshering.service.interfaces.CarClassService;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class CarClassServiceImpl implements CarClassService {
//
//    private final CarClassMapper carClassMapper;
//    private final CarClassRepository carClassRepository;
//
//    @Override
//    public ModelNameResponse createCarClass(CreateCarModelName request) {
//
//        CarClass saved = carClassRepository.save(carClassMapper.toEntity(request));
//
//        return carClassMapper.toDto(saved);
//    }
//
//    @Override
//    public CarClass getCarClassByName(String name) {
//        return carClassRepository.findByNameIgnoreCase(name)
//                .orElseThrow(() -> new NotFoundException("Car Class Not Found"));
//    }
//
//    @Override
//    public List<String> findAllClasses() {
//        return carClassRepository.findAll()
//                .stream()
//                .map(CarClass::getName)
//                .toList();
//    }
//}
