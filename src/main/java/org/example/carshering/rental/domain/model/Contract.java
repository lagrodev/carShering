package org.example.carshering.rental.domain.model;

import lombok.Getter;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.common.domain.valueobject.Money;
import org.example.carshering.common.exceptions.custom.ContractNotYetStartedException;
import org.example.carshering.common.exceptions.custom.InvalidContractStateException;
import org.example.carshering.rental.domain.valueobject.*;

import java.time.LocalDateTime;

@Getter
public class Contract {
    // Unique identifier for the contract
    // final fields
    private final ContractId id;
    private final CarId carId;
    private final ClientId clientId;

    // mutable fields
    private RentalPeriod rentalPeriod;
    private Money totalCost;
    private RentalStateType state;
    private String comment;

    public static Contract create(ClientId clientId, CarId carId,
                                  RentalPeriod period, Money dailyRate) {
        Money total = dailyRate.multiply(period.getDurationInDays());
        return new Contract(null, clientId, carId, period, total,
                RentalStateType.PENDING, null);
    }

    private Contract(ContractId id, ClientId clientId, CarId carId,
                     RentalPeriod rentalPeriod, Money totalCost,
                     RentalStateType state, String comment) {
        this.id = id;
        this.clientId = clientId;
        this.carId = carId;
        this.rentalPeriod = rentalPeriod;
        this.totalCost = totalCost;
        this.state = state;
        this.comment = comment;
    }

    public static Contract restore(ContractId id, ClientId clientId, CarId carId,
                                   RentalPeriod period, Money totalCost,
                                   RentalStateType state, String comment) {
        return new Contract(id, clientId, carId, period, totalCost, state, comment);
    }

    public void confirm(){
        if (!state.canTransitionTo(RentalStateType.CONFIRMED)) {
            throw new InvalidContractStateException("Cannot confirm contract in state:" + state);
        }
        state = RentalStateType.CONFIRMED;
    }


    public void cancel(){
        if (!state.canTransitionTo(RentalStateType.CANCELLED)) {
            throw new InvalidContractStateException("Cannot cancel contract in state:" + state);
        }
        state = RentalStateType.CANCELLED;
    }



    public void requestCancellation() {
        if (!state.canTransitionTo(RentalStateType.CANCELLATION_REQUESTED)){
            throw new InvalidContractStateException("Cannot request cancellation in state: " + state);
        }
        state = RentalStateType.CANCELLATION_REQUESTED;
    }


    public void updateDates(RentalPeriod newPeriod, Money dailyRate) {
        if (!state.isUpdatable()) {
            throw new InvalidContractStateException("Cannot update dates in state: " + state);
        }
        this.rentalPeriod = newPeriod;
        this.totalCost = dailyRate.multiply(newPeriod.getDurationInDays());
    }


    public void activate() {
        if (!state.canTransitionTo(RentalStateType.ACTIVE)) {
            throw new InvalidContractStateException("Cannot activate contract in state: " + state);
        }

        if (rentalPeriod.getStartDate().isAfter(LocalDateTime.now())) {
            throw new ContractNotYetStartedException(
                    "Cannot activate contract before start date: " + rentalPeriod.getStartDate()
            );
        }

        state = RentalStateType.ACTIVE;
        // ивент в domain ivent
    }

    public void complete(){
        if (!state.canTransitionTo(RentalStateType.COMPLETED)) {
            throw new InvalidContractStateException("Cannot complete contract in state: " + state);
        }

        if (rentalPeriod.getEndDate().isAfter(LocalDateTime.now())) {
            throw new InvalidContractStateException(
                    "Cannot complete contract before end date: " + rentalPeriod.getEndDate()
            );
        }

        state = RentalStateType.COMPLETED;
        // ивент в domain ivent
    }
}
