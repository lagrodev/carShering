package org.example.carshering.dto.request;


public record FilterContractRequest(

        String status,   // true = только заблокированные, false = только активные, null = все
        Long idUser,
        Long idCar,
        String brand,
        String bodyType,
        String carClass
) {}
/*

{
  "banned": true,
  "roleName": null
}
*/
