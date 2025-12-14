package org.example.carshering.identity.domain.valueobject.role;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class RoleName {
    private final String value;

    private RoleName(String value) {
        this.value = value;
    }

    public static RoleName of(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be null");
        }
        return new RoleName(name.trim().toUpperCase());
    }
}
