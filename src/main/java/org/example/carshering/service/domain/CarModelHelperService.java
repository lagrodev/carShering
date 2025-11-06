package org.example.carshering.service.domain;

import lombok.RequiredArgsConstructor;
import org.example.carshering.entity.CarModel;
import org.example.carshering.exceptions.custom.CarNotFoundException;
import org.example.carshering.repository.CarModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarModelHelperService {

    private final CarModelRepository carModelRepository;

    @Transactional(readOnly = true)
    public CarModel getCarModelById(Long id) {
        return carModelRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException("Car model not found"));
    }

    @Transactional
    public CarModel save(CarModel model) {
        return carModelRepository.save(model);
    }

    @Transactional
    public void restoreIfDeleted(CarModel model) {
        if (model.isDeleted()) {
            model.setDeleted(false);
            carModelRepository.save(model);
        }
    }

}
