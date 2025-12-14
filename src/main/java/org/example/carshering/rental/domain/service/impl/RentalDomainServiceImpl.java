package org.example.carshering.rental.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.common.domain.valueobject.Money;
import org.example.carshering.rental.domain.model.Contract;
import org.example.carshering.rental.domain.repository.ContractDomainRepository;
import org.example.carshering.rental.domain.service.RentalDomainService;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.rental.domain.valueobject.ContractId;
import org.example.carshering.rental.domain.valueobject.RentalPeriod;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class RentalDomainServiceImpl  implements RentalDomainService {
    // Здесь можно добавить методы бизнес-логики, связанные с арендой автомобилей

    @Override
    public Money calculateCancellationFee(Contract contract) {
        if (canCanceledWithoutFee(contract)) {
            return Money.zero("rub");
        }
        // 30% от стоимости
        return contract.getTotalCost().multiply(BigDecimal.valueOf(0.3));
    }

    @Override
    public boolean canCanceledWithoutFee(Contract contract) {
        long daysUntilStart = ChronoUnit.DAYS.between(
                LocalDateTime.now(),
                contract.getRentalPeriod().getStartDate()
        );
        return daysUntilStart > 5 ; // Можно отменить без штрафа, если до начала аренды больше 5 дней
    }

    private final ContractDomainRepository contractRepository;

    @Override
    public boolean isCarAvailableForRental(CarId carId, RentalPeriod rentalPeriod, ContractId contractId) {
        return contractRepository.findOverlappingContracts(rentalPeriod, carId, contractId).isEmpty();
    }


}