package org.example.carshering.rental.domain.service;

import org.example.carshering.common.domain.valueobject.Money;
import org.example.carshering.rental.domain.model.Contract;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.rental.domain.valueobject.ContractId;
import org.example.carshering.rental.domain.valueobject.RentalPeriod;

public interface RentalDomainService {
    // Здесь можно добавить методы бизнес-логики, связанные с арендой автомобилей

    /**
     * Рассчитывает штраф за отмену контракта
     * @param contract контракт аренды
     * @return сумма штрафа
     */
    Money calculateCancellationFee(Contract contract);

    /**
     * Проверяет, можно ли отменить контракт без штрафа
     * @param contract контракт аренды
     * @return true, если можно отменить без штрафа, иначе false
     */
    boolean canCanceledWithoutFee(Contract contract);


    boolean isCarAvailableForRental(CarId carId, RentalPeriod rentalPeriod, ContractId contractId);
}
