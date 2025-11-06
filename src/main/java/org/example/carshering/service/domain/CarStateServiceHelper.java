package org.example.carshering.service.domain;

import lombok.RequiredArgsConstructor;
import org.example.carshering.entity.CarState;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.repository.CarStateRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarStateServiceHelper {
    private final CarStateRepository stateRepository;


    public CarState getStateByName(String stateName) {
        return stateRepository.findByStatusIgnoreCase(stateName)
                .orElseThrow(() -> new NotFoundException("State not found"));
    }
}
