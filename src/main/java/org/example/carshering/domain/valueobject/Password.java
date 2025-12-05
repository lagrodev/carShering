package org.example.carshering.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Value Object representing an encoded password.
 *
 * Important: This class stores ALREADY ENCODED password (BCrypt, etc.).
 * Password encoding should be done BEFORE creating this Value Object.
 *
 * This is NOT for plain text passwords!
 *
 * Immutable - cannot be changed after creation.
 */
@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class Password {
    private static final int MIN_ENCODED_LENGTH = 30; // BCrypt hash is ~60 chars

    private final String value;

    private Password(String value) {
        this.value = value;
    }

    /**
     * Creates Password value object from ENCODED password string.
     *
     * WARNING: Pass ONLY encoded password (BCrypt, etc.), NOT plain text!
     *
     * @param encodedPassword already encoded password (e.g., BCrypt hash)
     * @return Password value object
     * @throws IllegalArgumentException if password is invalid
     */
    public static Password ofEncoded(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new IllegalArgumentException("Encoded password cannot be null or blank");
        }

        String trimmed = encodedPassword.trim();

        if (trimmed.length() < MIN_ENCODED_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Invalid encoded password length: %d. Expected at least %d characters (BCrypt hash)",
                    trimmed.length(), MIN_ENCODED_LENGTH)
            );
        }

        return new Password(trimmed);
    }

    /**
     * Creates Password value object from plain text password with validation.
     *
     * This method validates plain password requirements and returns a wrapper.
     * The actual encoding should be done by PasswordEncoder before storage.
     *
     * Use this for validation before encoding, then use ofEncoded() after encoding.
     *
     * @param plainPassword plain text password
     * @return Password value object with plain password (FOR VALIDATION ONLY!)
     * @throws IllegalArgumentException if password doesn't meet requirements
     */
    public static Password validatePlain(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }

        String trimmed = plainPassword.trim();

        if (trimmed.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        if (trimmed.length() > 128) {
            throw new IllegalArgumentException("Password must not exceed 128 characters");
        }

        // Check for at least one digit
        if (!trimmed.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }

        // Check for at least one letter
        if (!trimmed.matches(".*[a-zA-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one letter");
        }

        // NOTE: This returns plain password for validation purposes
        // Caller must encode it before persisting!
        return new Password(trimmed);
    }

    /**
     * Checks if this is an encoded password (BCrypt format).
     */
    public boolean isEncoded() {
        return value.length() >= MIN_ENCODED_LENGTH && value.startsWith("$2");
    }

    @Override
    public String toString() {
        // Never expose password in toString!
        return "Password[***]";
    }
}

