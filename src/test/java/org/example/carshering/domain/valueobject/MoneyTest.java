package org.example.carshering.domain.valueobject;


import org.example.carshering.exceptions.custom.InvalidArgumentException;
import org.junit.jupiter. api.Test;
import java. math.BigDecimal;

import static org.junit.jupiter. api.Assertions.*;

public class MoneyTest {
    @Test
    void shouldCreateMoneyWithValidAmount() {
        Money money = Money.rubles(100.50);

        assertEquals(new BigDecimal("100.50"), money.getAmount());
        assertEquals("RUB", money.getCurrencyCode());
    }

    @Test
    void shouldRoundAmountToTwoDecimals() {
        Money money = Money.rubles(100.999);

        assertEquals(new BigDecimal("101.00"), money.getAmount());
    }

    @Test
    void shouldThrowExceptionForNegativeAmount() {
        assertThrows(InvalidArgumentException. class, () ->
                Money.rubles(-100)
        );
    }

    @Test
    void shouldThrowExceptionForNullAmount() {
        assertThrows(InvalidArgumentException.class, () ->
                Money.of(null, "RUB")
        );
    }

    @Test
    void shouldAddMoneyOfSameCurrency() {
        Money m1 = Money.rubles(100);
        Money m2 = Money.rubles(50);

        Money result = m1.add(m2);

        assertEquals(new BigDecimal("150.00"), result.getAmount());
    }

    @Test
    void shouldThrowExceptionWhenAddingDifferentCurrencies() {
        Money rubles = Money.of(BigDecimal.valueOf(100), "RUB");
        Money dollars = Money.of(BigDecimal.valueOf(100), "USD");

        assertThrows(InvalidArgumentException. class, () ->
                rubles.add(dollars)
        );
    }

    @Test
    void shouldSubtractMoney() {
        Money m1 = Money.rubles(100);
        Money m2 = Money.rubles(30);

        Money result = m1.subtract(m2);

        assertEquals(new BigDecimal("70.00"), result.getAmount());
    }

    @Test
    void shouldMultiplyByInteger() {
        Money money = Money.rubles(10);

        Money result = money.multiply(5);

        assertEquals(new BigDecimal("50.00"), result.getAmount());
    }

    @Test
    void shouldMultiplyByDecimal() {
        Money money = Money.rubles(100);

        Money result = money.multiply(BigDecimal.valueOf(0.1));

        assertEquals(new BigDecimal("10.00"), result.getAmount());
    }

    @Test
    void shouldCompareMoneyAmounts() {
        Money m1 = Money.rubles(100);
        Money m2 = Money.rubles(50);

        assertTrue(m1.isGreaterThan(m2));
        assertTrue(m2.isLessThan(m1));
    }

    @Test
    void shouldCheckIfZero() {
        Money zero = Money.zeroRubles();
        Money nonZero = Money.rubles(10);

        assertTrue(zero.isZero());
        assertFalse(nonZero. isZero());
    }

    @Test
    void shouldBeImmutable() {
        Money original = Money.rubles(100);
        Money result = original.add(Money.rubles(50));

        // Оригинал не изменился
        assertEquals(new BigDecimal("100.00"), original.getAmount());
        assertEquals(new BigDecimal("150.00"), result.getAmount());
    }
}
