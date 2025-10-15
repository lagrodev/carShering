package org.example.carshering.service.domain;

import lombok.RequiredArgsConstructor;
import org.example.carshering.entity.Car;
import org.example.carshering.entity.Contract;
import org.example.carshering.repository.ContractRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalDomainService {

    private final ContractRepository contractRepository;

    public boolean isCarAvailable(LocalDate start, LocalDate end, Long carId) {
        return contractRepository.findOverlappingContracts(start, end, carId).isEmpty();
    }

    public double calculateCost(Car car, LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end);
        return days * car.getRent();
    }
}
