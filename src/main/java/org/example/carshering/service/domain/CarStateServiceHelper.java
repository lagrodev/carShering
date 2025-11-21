package org.example.carshering.service.domain;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.example.carshering.entity.CarState;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.repository.CarStateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarStateServiceHelper {
    private final CarStateRepository stateRepository;


    public CarState getStateByName(String stateName) {
        return stateRepository.findByStatusIgnoreCase(stateName)
                .orElseThrow(() -> new NotFoundException("State not found"));
    }
    private static final String CAR_STATE_AVAILABLE = "AVAILABLE";


    public CarState getDefaultState() {
        return getStateByName(CAR_STATE_AVAILABLE);
    }

    public CarState getStateById(@NotNull Long stateId) {
        return stateRepository.findById(stateId).orElseThrow(() -> new NotFoundException("State not found"));
    }


}
