package org.example.carshering.domain.valueobject;


import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.carshering.exceptions.custom.InvalidArgumentException;

/**
 * Value Object representing an email address.
 * Validates email format according to RFC 5321.
 * Immutable - cannot be changed after creation.
 */
@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class Email {
    private final String value;

    private Email(String value) {
        this.value = value;
    }

    /**
     * Creates an Email value object from a string.
     * Email is normalized to lowercase and trimmed.
     *
     * @param value email address string
     * @return Email value object
     * @throws InvalidArgumentException if email format is invalid
     */
    public static Email of(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidArgumentException("Email cannot be null or blank");
        }

        String trimmed = value.trim().toLowerCase();

        // Проверка длины (RFC 5321)
        if (trimmed.length() > 254) {
            throw new InvalidArgumentException("Email is too long (max 254 characters)");
        }

        // Проверка наличия @ и разделение на local и domain части
        int atIndex = trimmed.indexOf('@');
        if (atIndex <= 0 || atIndex == trimmed.length() - 1) {
            throw new InvalidArgumentException("Invalid email format: missing or misplaced @");
        }

        // Проверка на множественные @
        if (trimmed.indexOf('@', atIndex + 1) != -1) {
            throw new InvalidArgumentException("Invalid email format: multiple @ symbols");
        }

        String localPart = trimmed.substring(0, atIndex);
        String domainPart = trimmed.substring(atIndex + 1);

        // Валидация локальной части (до @)
        validateLocalPart(localPart);

        // Валидация доменной части (после @)
        validateDomainPart(domainPart);

        return new Email(trimmed);
    }

    private static void validateLocalPart(String localPart) {
        // Длина локальной части (RFC 5321)
        // Максимум 64 символа
        if (localPart.length() > 64) {
            throw new InvalidArgumentException("Email local part is too long (max 64 characters)");
        }

        // Не должна начинаться или заканчиваться точкой
        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            throw new InvalidArgumentException("Email local part cannot start or end with a dot");
        }

        // Не должна содержать две точки подряд
        if (localPart.contains("..")) {
            throw new InvalidArgumentException("Email local part cannot contain consecutive dots");
        }

        // Разрешенные символы: a-z, 0-9, . _ + -
        String localPartRegex = "^[a-z0-9._+-]+$";
        if (!localPart.matches(localPartRegex)) {
            throw new InvalidArgumentException("Email local part contains invalid characters");
        }
    }

    private static void validateDomainPart(String domainPart) {
        // Длина доменной части
        if (domainPart.length() > 253) {
            throw new InvalidArgumentException("Email domain is too long (max 253 characters)");
        }

        // Должен содержать хотя бы одну точку
        if (!domainPart.contains(".")) {
            throw new InvalidArgumentException("Email domain must contain at least one dot");
        }

        // Не должен начинаться или заканчиваться точкой или дефисом
        if (domainPart.startsWith(".") || domainPart.endsWith(".") ||
            domainPart.startsWith("-") || domainPart.endsWith("-")) {
            throw new InvalidArgumentException("Email domain cannot start or end with dot or hyphen");
        }

        // Не должен содержать две точки подряд
        if (domainPart.contains("..")) {
            throw new InvalidArgumentException("Email domain cannot contain consecutive dots");
        }

        // Проверка каждого сегмента домена
        String[] domainParts = domainPart.split("\\.");
        for (String part : domainParts) {
            if (part.isEmpty()) {
                throw new InvalidArgumentException("Email domain contains empty segment");
            }
            if (part.length() > 63) {
                throw new InvalidArgumentException("Email domain segment is too long (max 63 characters)");
            }
            // Сегмент не должен начинаться или заканчиваться дефисом
            if (part.startsWith("-") || part.endsWith("-")) {
                throw new InvalidArgumentException("Email domain segment cannot start or end with hyphen");
            }
        }

        // Проверка TLD (последний сегмент должен быть не короче 2 символов и содержать только буквы)
        String tld = domainParts[domainParts.length - 1];
        if (tld.length() < 2) {
            throw new InvalidArgumentException("Email domain TLD is too short (min 2 characters)");
        }
        if (!tld.matches("^[a-z]+$")) {
            throw new InvalidArgumentException("Email domain TLD must contain only letters");
        }

        // Разрешенные символы в домене: a-z, 0-9, . -
        String domainRegex = "^[a-z0-9.-]+$";
        if (!domainPart.matches(domainRegex)) {
            throw new InvalidArgumentException("Email domain contains invalid characters");
        }
    }

    /**
     * Returns the domain part of the email (after @).
     * Example: "user@example.com" returns "example.com"
     */
    public String getDomain() {
        int atIndex = value.indexOf('@');
        return value.substring(atIndex + 1);
    }

    /**
     * Returns the local part of the email (before @).
     * Example: "user@example.com" returns "user"
     */
    public String getLocalPart() {
        int atIndex = value.indexOf('@');
        return value.substring(0, atIndex);
    }

    @Override
    public String toString() {
        return value;
    }

}
