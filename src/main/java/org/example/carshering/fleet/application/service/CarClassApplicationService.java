package org.example.carshering.fleet.application.service;

import org.example.carshering.dto.request.create.CreateCarModelName;
import org.example.carshering.fleet.api.dto.request.CreateBrandRequest;
import org.example.carshering.fleet.application.dto.response.CarClassDto;

import java.util.List;

public interface CarClassApplicationService {


    CarClassDto createCarClass(CreateCarModelName request);

    List<CarClassDto> getAllCarClass();
}
