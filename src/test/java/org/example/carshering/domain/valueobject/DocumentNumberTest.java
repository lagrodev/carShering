package org.example.carshering.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for DocumentNumber Value Object.
 */
class DocumentNumberTest {

    @Test
    void shouldCreateValidDocumentNumber() {
        // Given
        String number = "123456";

        // When
        DocumentNumber documentNumber = DocumentNumber.of(number);

        // Then
        assertThat(documentNumber.getValue()).isEqualTo("123456");
    }

    @Test
    void shouldTrimWhitespace() {
        // Given
        String number = "  123456  ";

        // When
        DocumentNumber documentNumber = DocumentNumber.of(number);

        // Then
        assertThat(documentNumber.getValue()).isEqualTo("123456");
    }

    @Test
    void shouldThrowExceptionWhenNumberIsNull() {
        // When & Then
        assertThatThrownBy(() -> DocumentNumber.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Document number cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenNumberIsBlank() {
        // When & Then
        assertThatThrownBy(() -> DocumentNumber.of("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Document number cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenNumberIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> DocumentNumber.of(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Document number cannot be null or blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "1234", "123"})
    void shouldThrowExceptionWhenNumberTooShort(String shortNumber) {
        // When & Then
        assertThatThrownBy(() -> DocumentNumber.of(shortNumber))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid document number format")
            .hasMessageContaining("Expected 6-10 digits");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678901"})
    void shouldThrowExceptionWhenNumberTooLong(String longNumber) {
        // When & Then
        assertThatThrownBy(() -> DocumentNumber.of(longNumber))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid document number format")
            .hasMessageContaining("Expected 6-10 digits");
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456", "1234567", "12345678", "123456789", "1234567890"})
    void shouldAcceptValidNumbersWithDifferentLengths(String validNumber) {
        // When
        DocumentNumber number = DocumentNumber.of(validNumber);

        // Then
        assertThat(number.getValue()).isEqualTo(validNumber);
    }

    @ParameterizedTest
    @ValueSource(strings = {"12A456", "ABC123", "123-456", "123 456"})
    void shouldThrowExceptionWhenNumberContainsInvalidCharacters(String invalidNumber) {
        // When & Then
        assertThatThrownBy(() -> DocumentNumber.of(invalidNumber))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid document number format");
    }

    @Test
    void shouldMaskNumber() {
        // Given
        DocumentNumber number = DocumentNumber.of("123456");

        // When
        String masked = number.getMasked();

        // Then
        assertThat(masked).isEqualTo("**3456");
    }

    @Test
    void shouldMaskLongNumber() {
        // Given
        DocumentNumber number = DocumentNumber.of("1234567890");

        // When
        String masked = number.getMasked();

        // Then
        assertThat(masked).isEqualTo("******7890");
    }

    @Test
    void shouldNotMaskShortNumber() {
        // Given - exactly 4 digits (edge case, shouldn't happen with validation)
        // We'll use 6 digits which is minimum
        DocumentNumber number = DocumentNumber.of("123456");

        // When
        String masked = number.getMasked();

        // Then
        assertThat(masked).hasSize(6);
        assertThat(masked).endsWith("3456");
    }

    @Test
    void shouldBeEqualWhenNumbersAreSame() {
        // Given
        DocumentNumber number1 = DocumentNumber.of("123456");
        DocumentNumber number2 = DocumentNumber.of(" 123456 ");

        // Then
        assertThat(number1).isEqualTo(number2);
        assertThat(number1.hashCode()).isEqualTo(number2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenNumbersDiffer() {
        // Given
        DocumentNumber number1 = DocumentNumber.of("123456");
        DocumentNumber number2 = DocumentNumber.of("123457");

        // Then
        assertThat(number1).isNotEqualTo(number2);
    }

    @Test
    void shouldReturnValueInToString() {
        // Given
        DocumentNumber number = DocumentNumber.of("123456");

        // When
        String result = number.toString();

        // Then
        assertThat(result).isEqualTo("123456");
    }

    @Test
    void shouldMaskPreserveLastFourDigits() {
        // Given
        DocumentNumber number = DocumentNumber.of("9876543210");

        // When
        String masked = number.getMasked();

        // Then
        assertThat(masked).isEqualTo("******3210");
        assertThat(masked).endsWith("3210");
    }
}
