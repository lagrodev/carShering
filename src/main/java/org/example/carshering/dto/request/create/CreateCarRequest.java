package org.example.carshering.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCarRequest(
        @NotNull Long modelId,
        @NotNull Integer yearOfIssue,
        @NotBlank String gosNumber,
        @NotBlank String vin,
        @NotNull Double rent
/*
{
  "modelId": "1",
  "yearOfIssue": "2009",
  "gosNumber": "23312",
  "vin": "123",
  "rent": "463746394"
}

*/

) {
}
