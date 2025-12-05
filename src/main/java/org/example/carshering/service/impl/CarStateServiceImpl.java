package org.example.carshering.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.response.CarStateResponse;
import org.example.carshering.domain.entity.CarState;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.mapper.CarStateMapper;
import org.example.carshering.repository.CarStateRepository;
import org.example.carshering.service.interfaces.CarStateService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarStateServiceImpl implements CarStateService {


    private final CarStateRepository stateRepository;
    private final CarStateMapper stateMapper;

    @Override
    public List<CarStateResponse> getAllStates() {
        return stateRepository.findAll().
                stream()
                .map(stateMapper::toDto)
                .toList();
    }

    @Override
    public CarState getStateByName(String stateName) {
        return stateRepository.findByStatusIgnoreCase(stateName)
                .orElseThrow(() -> new NotFoundException("State not found"));
    }
}
