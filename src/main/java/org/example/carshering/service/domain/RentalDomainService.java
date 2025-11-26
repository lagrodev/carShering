package org.example.carshering.service.domain;

import lombok.RequiredArgsConstructor;
import org.example.carshering.entity.Car;
import org.example.carshering.entity.Contract;
import org.example.carshering.repository.ContractRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalDomainService {

    private final ContractRepository contractRepository;

    public boolean isCarAvailable(LocalDateTime start, LocalDateTime end, Long carId, Long excludeContractId) {
        return contractRepository.findOverlappingContracts(start, end, carId, excludeContractId).isEmpty();
    }


    public double calculateCost(Car car, LocalDateTime start, LocalDateTime end) {
        long hours = Math.max(1, ChronoUnit.HOURS.between(start, end)); // минимум 1 час
        return hours * car.getRent();
    }
}
