package org.example.carshering.rental.domain.valueobject;

import lombok.Getter;

@Getter
public enum RentalStateType {
    PENDING("Ожидает подтверждения"){
        @Override
        public boolean canTransitionTo(RentalStateType target) {
            return target == CONFIRMED || target == CANCELLATION_REQUESTED;
        }

        @Override
        public boolean isUpdatable() {
            return true; // Можно изменять даты до подтверждения
        }
    },
    CONFIRMED("Подтверждён"){
        @Override
        public boolean canTransitionTo(RentalStateType target) {
            return target == ACTIVE || target == CANCELLATION_REQUESTED;
        }

        @Override
        public boolean isUpdatable() {
            return true; // Можно изменять даты до начала аренды
        }
    },
    ACTIVE("Активен") {
        @Override
        public boolean canTransitionTo(RentalStateType target) {
            return target == COMPLETED || target == CANCELLATION_REQUESTED;
        }

        @Override
        public boolean isUpdatable() {
            return false; // Нельзя изменять активную аренду
        }
    },
    COMPLETED("Завершён") {
        @Override
        public boolean canTransitionTo(RentalStateType target) {
            return false;
        }

        @Override
        public boolean isUpdatable() {
            return false; // Завершённую аренду нельзя изменить
        }
    },
    CANCELLED("Отменён") {
        @Override
        public boolean canTransitionTo(RentalStateType target) {
            return false;
        }

        @Override
        public boolean isUpdatable() {
            return false; // Отменённую аренду нельзя изменить
        }
    },
    CANCELLATION_REQUESTED("Запрошена отмена") {
        @Override
        public boolean canTransitionTo(RentalStateType target) {
            return target == CANCELLED || target == ACTIVE;
        }

        @Override
        public boolean isUpdatable() {
            return false; // Нельзя изменять при запросе отмены
        }
    };

    public abstract boolean canTransitionTo(RentalStateType target);

    public abstract boolean isUpdatable();

    private final String description;

    RentalStateType(String description) {
        this.description = description;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }


    public boolean isConfirmed() {
        return this == CONFIRMED;
    }
}
