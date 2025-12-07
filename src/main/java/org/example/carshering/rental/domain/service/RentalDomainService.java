package org.example.carshering.rental.domain.service;

import org.example.carshering.common.domain.valueobject.Money;
import org.example.carshering.rental.domain.model.Contract;
import org.example.carshering.rental.domain.valueobject.RentalPeriod;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class RentalDomainService {
    // Здесь можно добавить методы бизнес-логики, связанные с арендой автомобилей
    public Money calculateTotalCost(Money dailyRate, int durationInDays) {
        return dailyRate.multiply(durationInDays); // Пример расчёта общей стоимости аренды, можно будет потом  сделать, чтобы были некие штрафы?
    }


    public boolean canCanceledWithoutFee(Contract contract) {
        long daysUntilStart = ChronoUnit.DAYS.between(
                LocalDateTime.now(),
                contract.getRentalPeriod().getStartDate()
        );
        return daysUntilStart > 5 ; // Можно отменить без штрафа, если до начала аренды больше 5 дней
    }

    // что ещё может понадобиться?
    // да, штрафы, прикольная тема


}