package org.example.carshering.service.domain;

import lombok.RequiredArgsConstructor;
import org.example.carshering.domain.valueobject.Money;
import org.example.carshering.domain.entity.Car;
import org.example.carshering.repository.ContractRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class RentalDomainService {

    private final ContractRepository contractRepository;

    public boolean isCarAvailable(LocalDateTime start, LocalDateTime end, Long carId, Long excludeContractId) {
        return contractRepository.findOverlappingContracts(start, end, carId, excludeContractId).isEmpty();
    }


    public Money calculateCost(Car car, LocalDateTime start, LocalDateTime end) {
        long hours = Math.max(1, ChronoUnit.HOURS.between(start, end));
        return car.getDailyRate().multiply(hours);
    }
}
