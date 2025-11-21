package org.example.carshering.service.interfaces;

import org.example.carshering.dto.response.CarStateResponse;
import org.example.carshering.entity.CarState;

import java.util.List;

public interface CarStateService {
    List<CarStateResponse> getAllStates();

    CarState getStateByName(String stateName);
}
