package org.example.carshering.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// dto/request/UpdateCarRequest.java
public record UpdateCarRequest(
        @NotNull Long modelId,
        @NotNull Integer yearOfIssue,
        @NotBlank String gosNumber,
        @NotBlank String vin,
        @NotNull Double rent
) {}