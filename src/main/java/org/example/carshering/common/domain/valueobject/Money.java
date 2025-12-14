package org.example.carshering.common.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.carshering.common.exceptions.custom.InvalidArgumentException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Money {
    private final BigDecimal amount;
    private final String currencyCode;

    private Money(BigDecimal amount, String currencyCode) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currencyCode = currencyCode;
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, (currencyCode));
    }

    public static Money rubles(BigDecimal amount) {
        return of(amount, "RUB");
    }

    public static Money rubles(double amount) {
        return rubles(BigDecimal.valueOf(amount));
    }

    public static Money rubles(long amount) {
        return rubles(BigDecimal.valueOf(amount));
    }

    public static Money zero(String currencyCode) {
        return of(BigDecimal.ZERO, currencyCode);
    }

    public static Money zeroRubles() {
        return zero("RUB");
    }

    // helper method to validate currency match
    private void validateCurrencyMatch(Money other) {
        if (!this.currencyCode.equals(other.currencyCode)) {
            throw new InvalidArgumentException(String.format(
                    "Cannot operate with different currencies: %s and %s",
                    this.currencyCode,
                    other.currencyCode));
        }
    }

    // arithmetic operations
    public Money add(Money other) {
        validateCurrencyMatch(other);
        return new Money(this.amount.add(other.amount), this.currencyCode);
    }

    public Money subtract(Money other) {
        validateCurrencyMatch(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidArgumentException("Resulting amount cannot be negative");
        }
        return new Money(result, this.currencyCode);
    }

    public Money multiply(long factor) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currencyCode);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), this.currencyCode);
    }


    // comparison operations
    public boolean isGreaterThan(Money other) {
        validateCurrencyMatch(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        validateCurrencyMatch(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public String toString() {
        return String.format("%s %s", amount, currencyCode);
    }

}
