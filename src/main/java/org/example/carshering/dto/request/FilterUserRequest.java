package org.example.carshering.dto.request;


public record FilterUserRequest(
        Boolean banned,   // true = только заблокированные, false = только активные, null = все
        String roleName   // null = все роли

) {}
/*

{
  "banned": true,
  "roleName": null
}
*/
