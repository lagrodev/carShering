package org.example.carshering.domain.valueobject;

import org.example.carshering.identity.domain.valueobject.user.Password;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Password Value Object.
 */
class PasswordTest {

    // Example of BCrypt encoded password
    private static final String ENCODED_PASSWORD = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @Test
    void shouldCreatePasswordFromEncodedString() {
        // Given
        String encoded = ENCODED_PASSWORD;

        // When
        Password password = Password.ofEncoded(encoded);

        // Then
        assertThat(password.getValue()).isEqualTo(encoded);
        assertThat(password.isEncoded()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenEncodedPasswordIsNull() {
        // When & Then
        assertThatThrownBy(() -> Password.ofEncoded(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Encoded password cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenEncodedPasswordIsBlank() {
        // When & Then
        assertThatThrownBy(() -> Password.ofEncoded("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Encoded password cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenEncodedPasswordTooShort() {
        // Given - too short to be a valid encoded password
        String shortPassword = "shortpass";

        // When & Then
        assertThatThrownBy(() -> Password.ofEncoded(shortPassword))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid encoded password length");
    }

    @Test
    void shouldValidatePlainPasswordWithValidInput() {
        // Given
        String plain = "Password123";

        // When
        Password password = Password.validatePlain(plain);

        // Then
        assertThat(password.getValue()).isEqualTo(plain);
        assertThat(password.isEncoded()).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenPlainPasswordIsNull() {
        // When & Then
        assertThatThrownBy(() -> Password.validatePlain(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Password cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenPlainPasswordIsBlank() {
        // When & Then
        assertThatThrownBy(() -> Password.validatePlain("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Password cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenPlainPasswordTooShort() {
        // Given - less than 8 characters
        String shortPassword = "Pass1";

        // When & Then
        assertThatThrownBy(() -> Password.validatePlain(shortPassword))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Password must be at least 8 characters long");
    }

    @Test
    void shouldThrowExceptionWhenPlainPasswordTooLong() {
        // Given - more than 128 characters
        String longPassword = "a".repeat(129) + "1";

        // When & Then
        assertThatThrownBy(() -> Password.validatePlain(longPassword))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Password must not exceed 128 characters");
    }

    @Test
    void shouldThrowExceptionWhenPlainPasswordHasNoDigit() {
        // Given - no digits
        String noDigit = "PasswordOnly";

        // When & Then
        assertThatThrownBy(() -> Password.validatePlain(noDigit))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Password must contain at least one digit");
    }

    @Test
    void shouldThrowExceptionWhenPlainPasswordHasNoLetter() {
        // Given - no letters
        String noLetter = "12345678";

        // When & Then
        assertThatThrownBy(() -> Password.validatePlain(noLetter))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Password must contain at least one letter");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Password1",
        "MyPass123",
        "Secure456",
        "Test1234",
        "abcdefg1"
    })
    void shouldAcceptValidPlainPasswords(String validPassword) {
        // When
        Password password = Password.validatePlain(validPassword);

        // Then
        assertThat(password.getValue()).isEqualTo(validPassword);
    }

    @Test
    void shouldTrimWhitespaceFromEncodedPassword() {
        // Given
        String encoded = "  " + ENCODED_PASSWORD + "  ";

        // When
        Password password = Password.ofEncoded(encoded);

        // Then
        assertThat(password.getValue()).isEqualTo(ENCODED_PASSWORD);
    }

    @Test
    void shouldTrimWhitespaceFromPlainPassword() {
        // Given
        String plain = "  Password123  ";

        // When
        Password password = Password.validatePlain(plain);

        // Then
        assertThat(password.getValue()).isEqualTo("Password123");
    }

    @Test
    void shouldDetectEncodedPassword() {
        // Given
        Password encoded = Password.ofEncoded(ENCODED_PASSWORD);

        // When & Then
        assertThat(encoded.isEncoded()).isTrue();
    }

    @Test
    void shouldDetectPlainPassword() {
        // Given
        Password plain = Password.validatePlain("Password123");

        // When & Then
        assertThat(plain.isEncoded()).isFalse();
    }

    @Test
    void shouldNotExposePasswordInToString() {
        // Given
        Password password = Password.ofEncoded(ENCODED_PASSWORD);

        // When
        String result = password.toString();

        // Then
        assertThat(result).isEqualTo("Password[***]");
        assertThat(result).doesNotContain(ENCODED_PASSWORD);
    }

    @Test
    void shouldBeEqualWhenPasswordsAreSame() {
        // Given
        Password password1 = Password.ofEncoded(ENCODED_PASSWORD);
        Password password2 = Password.ofEncoded(" " + ENCODED_PASSWORD + " ");

        // Then
        assertThat(password1).isEqualTo(password2);
        assertThat(password1.hashCode()).isEqualTo(password2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenPasswordsDiffer() {
        // Given
        Password password1 = Password.ofEncoded(ENCODED_PASSWORD);
        Password password2 = Password.ofEncoded(
            "$2a$10$DifferentEncodedPasswordHashHereXXXXXXXXXXXXXXXXXXXXX"
        );

        // Then
        assertThat(password1).isNotEqualTo(password2);
    }

    @Test
    void shouldAcceptExactly8CharactersWithDigitAndLetter() {
        // Given - minimum valid length
        String password = "Pass1234";

        // When
        Password result = Password.validatePlain(password);

        // Then
        assertThat(result.getValue()).isEqualTo(password);
    }

    @Test
    void shouldAcceptExactly128Characters() {
        // Given - maximum valid length
        String password = "a".repeat(127) + "1";

        // When
        Password result = Password.validatePlain(password);

        // Then
        assertThat(result.getValue()).hasSize(128);
    }

    @Test
    void shouldAcceptPasswordWithSpecialCharacters() {
        // Given - password with special chars
        String password = "Pass123!@#$";

        // When
        Password result = Password.validatePlain(password);

        // Then
        assertThat(result.getValue()).isEqualTo(password);
    }

    @Test
    void shouldAcceptPasswordWithUpperAndLowerCase() {
        // Given
        String password = "PaSsWoRd123";

        // When
        Password result = Password.validatePlain(password);

        // Then
        assertThat(result.getValue()).isEqualTo(password);
    }
}

