package org.example.carshering.service.interfaces;

import org.example.carshering.fleet.api.dto.responce.CarStateResponse;
import org.example.carshering.fleet.infrastructure.persistence.entity.CarState;

import java.util.List;

public interface CarStateService {
    List<CarStateResponse> getAllStates();

    CarState getStateByName(String stateName);
}
