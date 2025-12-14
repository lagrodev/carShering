package org.example.carshering.domain.valueobject;

import org.example.carshering.common.exceptions.custom.InvalidArgumentException;
import org.example.carshering.identity.domain.valueobject.user.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Email Value Object Tests")
class EmailTest {

    @Test
    @DisplayName("Should create valid email")
    void shouldCreateValidEmail() {
        // Given
        String validEmail = "test@example.com";

        // When
        Email email = Email.of(validEmail);

        // Then
        assertNotNull(email);
        assertEquals("test@example.com", email.getValue());
    }

    @Test
    @DisplayName("Should normalize email to lowercase")
    void shouldNormalizeEmailToLowercase() {
        // Given
        String mixedCaseEmail = "Test.User@Example.COM";

        // When
        Email email = Email.of(mixedCaseEmail);

        // Then
        assertEquals("test.user@example.com", email.getValue());
    }

    @Test
    @DisplayName("Should trim whitespace from email")
    void shouldTrimWhitespace() {
        // Given
        String emailWithSpaces = "  test@example.com  ";

        // When
        Email email = Email.of(emailWithSpaces);

        // Then
        assertEquals("test@example.com", email.getValue());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should throw exception for null or blank email")
    void shouldThrowExceptionForNullOrBlank(String invalidEmail) {
        // When/Then
        InvalidArgumentException exception = assertThrows(
            InvalidArgumentException.class,
            () -> Email.of(invalidEmail)
        );
        assertEquals("Email cannot be null or blank", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "user@example.com",
        "user.name@example.com",
        "user+tag@example.co.uk",
        "user_name@example.com",
        "user%test@example.com",
        "user-name@example.com",
        "u@example.com",
        "123@example.com",
        "user@sub.example.com",
        "user@sub.sub.example.com"
    })
    @DisplayName("Should accept valid email formats")
    void shouldAcceptValidEmails(String validEmail) {
        // When/Then
        assertDoesNotThrow(() -> Email.of(validEmail));
    }

    @Test
    @DisplayName("Should throw exception for email exceeding max length (254)")
    void shouldThrowExceptionForTooLongEmail() {
        // Given
        String longLocal = "a".repeat(64);
        String longDomain = "b".repeat(180) + ".com";
        String tooLongEmail = longLocal + "@" + longDomain;

        // When/Then
        InvalidArgumentException exception = assertThrows(
            InvalidArgumentException.class,
            () -> Email.of(tooLongEmail)
        );
        assertEquals("Email is too long (max 254 characters)", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid",
        "invalid@",
        "@invalid.com",
        "invalid@.com",
        "invalid@com",
        "invalid..user@example.com",
        ".invalid@example.com",
        "invalid.@example.com",
        "invalid@example",
        "invalid@example.",
        "invalid@.example.com",
        "invalid@example..com",
        "invalid@-example.com",
        "invalid@example-.com",
        "user@@example.com",
        "user@exam ple.com",
        "user @example.com",
        "user@example.c"
    })
    @DisplayName("Should reject invalid email formats")
    void shouldRejectInvalidEmails(String invalidEmail) {
        // When/Then
        assertThrows(InvalidArgumentException.class, () -> Email.of(invalidEmail));
    }

    @Test
    @DisplayName("Should throw exception for email without @")
    void shouldThrowExceptionForMissingAt() {
        // Given
        String emailWithoutAt = "userexample.com";

        // When/Then
        InvalidArgumentException exception = assertThrows(
            InvalidArgumentException.class,
            () -> Email.of(emailWithoutAt)
        );
        assertTrue(exception.getMessage().contains("missing or misplaced @"));
    }

    @Test
    @DisplayName("Should throw exception for multiple @ symbols")
    void shouldThrowExceptionForMultipleAt() {
        // Given
        String emailWithMultipleAt = "user@@example.com";

        // When/Then
        InvalidArgumentException exception = assertThrows(
            InvalidArgumentException.class,
            () -> Email.of(emailWithMultipleAt)
        );
        assertTrue(exception.getMessage().contains("multiple @ symbols"));
    }

    @Test
    @DisplayName("Should throw exception for local part exceeding 64 characters")
    void shouldThrowExceptionForTooLongLocalPart() {
        // Given
        String longLocal = "a".repeat(65);
        String email = longLocal + "@example.com";

        // When/Then
        InvalidArgumentException exception = assertThrows(
            InvalidArgumentException.class,
            () -> Email.of(email)
        );
        assertEquals("Email local part is too long (max 64 characters)", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for local part starting with dot")
    void shouldThrowExceptionForLocalPartStartingWithDot() {
        // Given
        String email = ".user@example.com";

        // When/Then
        InvalidArgumentException exception = assertThrows(
            InvalidArgumentException.class,
            () -> Email.of(email)
        );
        assertTrue(exception.getMessage().contains("cannot start or end with a dot"));
    }

    @Test
    @DisplayName("Should throw exception for local part ending with dot")
    void shouldThrowExceptionForLocalPartEndingWithDot() {
        // Given
        String email = "user.@example.com";

        // When/Then
        InvalidArgumentException exception = assertThrows(
            InvalidArgumentException.class,
            () -> Email.of(email)
        );
        assertTrue(exception.getMessage().contains("cannot start or end with a dot"));
    }

    @Test
    @DisplayName("Should throw exception for consecutive dots in local part")
    void shouldThrowExceptionForConsecutiveDotsInLocalPart() {
        // Given
        String email = "user..name@example.com";

        // When/Then
        InvalidArgumentException exception = assertThrows(
            InvalidArgumentException.class,
            () -> Email.of(email)
        );
        assertTrue(exception.getMessage().contains("cannot contain consecutive dots"));
    }

    @Test
    @DisplayName("Should throw exception for invalid characters in local part")
    void shouldThrowExceptionForInvalidCharsInLocalPart() {
        // Given
        String email = "user#name@example.com";

        // When/Then
        InvalidArgumentException exception = assertThrows(
            InvalidArgumentException.class,
            () -> Email.of(email)
        );
        assertTrue(exception.getMessage().contains("contains invalid characters"));
    }

    @Test
    @DisplayName("Should throw exception for domain without dot")
    void shouldThrowExceptionForDomainWithoutDot() {
        // Given
        String email = "user@localhost";

        // When/Then
        InvalidArgumentException exception = assertThrows(
            InvalidArgumentException.class,
            () -> Email.of(email)
        );
        assertTrue(exception.getMessage().contains("must contain at least one dot"));
    }

    @Test
    @DisplayName("Should throw exception for domain exceeding 253 characters")
    void shouldThrowExceptionForTooLongDomain() {
        // Given
        String longDomain = "a".repeat(250) + ".com";
        String email = "user@" + longDomain;

        // When/Then
        InvalidArgumentException exception = assertThrows(
            InvalidArgumentException.class,
            () -> Email.of(email)
        );
        assertTrue(exception.getMessage().contains("domain is too long"));
    }

    @Test
    @DisplayName("Should throw exception for domain starting with hyphen")
    void shouldThrowExceptionForDomainStartingWithHyphen() {
        // Given
        String email = "user@-example.com";

        // When/Then
        InvalidArgumentException exception = assertThrows(
            InvalidArgumentException.class,
            () -> Email.of(email)
        );
        assertTrue(exception.getMessage().contains("cannot start or end with dot or hyphen"));
    }

    @Test
    @DisplayName("Should throw exception for domain ending with hyphen")
    void shouldThrowExceptionForDomainEndingWithHyphen() {
        // Given
        String email = "user@example-.com";

        // When/Then
        InvalidArgumentException exception = assertThrows(
            InvalidArgumentException.class,
            () -> Email.of(email)
        );
        assertTrue(exception.getMessage().contains("cannot start or end with hyphen"));
    }

    @Test
    @DisplayName("Should throw exception for domain segment exceeding 63 characters")
    void shouldThrowExceptionForTooLongDomainSegment() {
        // Given
        String longSegment = "a".repeat(64);
        String email = "user@" + longSegment + ".com";

        // When/Then
        InvalidArgumentException exception = assertThrows(
            InvalidArgumentException.class,
            () -> Email.of(email)
        );
        assertTrue(exception.getMessage().contains("segment is too long"));
    }

    @Test
    @DisplayName("Should throw exception for TLD shorter than 2 characters")
    void shouldThrowExceptionForTooShortTld() {
        // Given
        String email = "user@example.c";

        // When/Then
        InvalidArgumentException exception = assertThrows(
            InvalidArgumentException.class,
            () -> Email.of(email)
        );
        assertTrue(exception.getMessage().contains("TLD is too short"));
    }

    @Test
    @DisplayName("Should throw exception for TLD with numbers")
    void shouldThrowExceptionForTldWithNumbers() {
        // Given
        String email = "user@example.c0m";

        // When/Then
        InvalidArgumentException exception = assertThrows(
            InvalidArgumentException.class,
            () -> Email.of(email)
        );
        assertTrue(exception.getMessage().contains("TLD must contain only letters"));
    }

    @Test
    @DisplayName("Should accept emails with subdomain")
    void shouldAcceptEmailsWithSubdomain() {
        // Given
        String email = "user@mail.example.com";

        // When
        Email result = Email.of(email);

        // Then
        assertNotNull(result);
        assertEquals("user@mail.example.com", result.getValue());
    }

    @Test
    @DisplayName("Should accept emails with multiple subdomains")
    void shouldAcceptEmailsWithMultipleSubdomains() {
        // Given
        String email = "user@mail.server.example.com";

        // When
        Email result = Email.of(email);

        // Then
        assertNotNull(result);
        assertEquals("user@mail.server.example.com", result.getValue());
    }

    @Test
    @DisplayName("Should accept email with all allowed special characters in local part")
    void shouldAcceptAllAllowedSpecialChars() {
        // Given
        String email = "user.name+tag_test%value@example.com";

        // When
        Email result = Email.of(email);

        // Then
        assertNotNull(result);
        assertEquals("user.name+tag_test%value@example.com", result.getValue());
    }
}

