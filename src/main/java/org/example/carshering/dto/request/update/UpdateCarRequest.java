package org.example.carshering.dto.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record UpdateCarRequest(
         Long modelId,
         Integer yearOfIssue,
         String gosNumber,
         String vin,
        @Positive Double rent
) {}