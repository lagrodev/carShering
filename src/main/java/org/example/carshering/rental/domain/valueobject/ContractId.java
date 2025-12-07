package org.example.carshering.rental.domain.valueobject;

import java.util.Objects;

// common/domain/valueobject/ContractId.java
public record ContractId(Long value) {
    public ContractId {
        Objects.requireNonNull(value, "ContractId cannot be null");
    }
}
