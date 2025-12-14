package org.example.carshering.identity.domain.valueobject.user;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Getter
public class Login {
    private final String value;

    private Login(String value) {
        this.value = value;
    }

    public static Login of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Login cannot be null or blank");
        }

        String trimmed = value.trim();

        if (trimmed.length() < 3 || trimmed.length() > 50) {
            throw new IllegalArgumentException("Login must be between 3 and 50 characters");
        }

        return new Login(trimmed);
    }


}
