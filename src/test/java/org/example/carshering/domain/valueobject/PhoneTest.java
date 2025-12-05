package org.example.carshering.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Phone Value Object.
 * Tests validation, normalization, and formatting.
 */
class PhoneTest {

    @Test
    void shouldCreateValidPhoneWithPlusSeven() {
        // Given
        String phoneNumber = "+79991234567";

        // When
        Phone phone = Phone.of(phoneNumber);

        // Then
        assertThat(phone.getValue()).isEqualTo("+79991234567");
    }

    @Test
    void shouldCreateValidPhoneWithEight() {
        // Given
        String phoneNumber = "89991234567";

        // When
        Phone phone = Phone.of(phoneNumber);

        // Then
        assertThat(phone.getValue()).isEqualTo("89991234567");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "+7 999 123 45 67",
        "+7-999-123-45-67",
        "+7 (999) 123-45-67",
        "+7(999)123-45-67",
        "+7 999-123-45-67"
    })
    void shouldNormalizePhoneWithPlusSevenVariants(String input) {
        // When
        Phone phone = Phone.of(input);

        // Then
        assertThat(phone.getValue()).isEqualTo("+79991234567");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "8 999 123 45 67",
        "8-999-123-45-67",
        "8 (999) 123-45-67",
        "8(999)123-45-67",
        "8 999-123-45-67"
    })
    void shouldNormalizePhoneWithEightVariants(String input) {
        // When
        Phone phone = Phone.of(input);

        // Then
        assertThat(phone.getValue()).isEqualTo("89991234567");
    }

    @Test
    void shouldTrimWhitespace() {
        // Given
        String phoneNumber = "  +79991234567  ";

        // When
        Phone phone = Phone.of(phoneNumber);

        // Then
        assertThat(phone.getValue()).isEqualTo("+79991234567");
    }

    @Test
    void shouldFormatPhoneForDisplay() {
        // Given
        Phone phone = Phone.of("+79991234567");

        // When
        String formatted = phone.getFormatted();

        // Then
        assertThat(formatted).isEqualTo("+7 (999) 123-45-67");
    }

    @Test
    void shouldFormatPhoneWithEightForDisplay() {
        // Given
        Phone phone = Phone.of("89991234567");

        // When
        String formatted = phone.getFormatted();

        // Then
        assertThat(formatted).isEqualTo("8 (999) 123-45-67");
    }

    @Test
    void shouldThrowExceptionWhenPhoneIsNull() {
        // When & Then
        assertThatThrownBy(() -> Phone.of(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Phone value cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenPhoneIsBlank() {
        // When & Then
        assertThatThrownBy(() -> Phone.of("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Phone cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenPhoneIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> Phone.of(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Phone cannot be null or blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "123456789",           // Too short
        "+7999123456",         // Too short (11 digits instead of 10 after +7)
        "+799912345678",       // Too long
        "79991234567",         // Missing + or starting with 7
        "99991234567",         // Invalid prefix
        "+89991234567",        // Invalid prefix (+8)
        "1234567890",          // Wrong prefix
        "+1234567890",         // Wrong country code
        "abc123456789",        // Contains letters
        "+7 999 abc 45 67"     // Contains letters
    })
    void shouldThrowExceptionForInvalidPhoneFormats(String invalidPhone) {
        // When & Then
        assertThatThrownBy(() -> Phone.of(invalidPhone))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid phone format");
    }

    @Test
    void shouldBeEqualWhenPhonesAreSame() {
        // Given
        Phone phone1 = Phone.of("+79991234567");
        Phone phone2 = Phone.of("+7 999 123 45 67"); // Different format, same normalized value

        // Then
        assertThat(phone1).isEqualTo(phone2);
        assertThat(phone1.hashCode()).isEqualTo(phone2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenPhonesAreDifferent() {
        // Given
        Phone phone1 = Phone.of("+79991234567");
        Phone phone2 = Phone.of("+79991234568");

        // Then
        assertThat(phone1).isNotEqualTo(phone2);
    }

    @Test
    void shouldReturnValueInToString() {
        // Given
        Phone phone = Phone.of("+79991234567");

        // When
        String result = phone.toString();

        // Then
        assertThat(result).isEqualTo("+79991234567");
    }

    @Test
    void shouldBeImmutable() {
        // Given
        Phone phone = Phone.of("+79991234567");
        String originalValue = phone.getValue();

        // When - try to get value multiple times
        phone.getValue();
        phone.getFormatted();

        // Then - value should remain the same
        assertThat(phone.getValue()).isEqualTo(originalValue);
    }
}

