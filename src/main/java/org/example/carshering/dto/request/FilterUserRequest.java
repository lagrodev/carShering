package org.example.carshering.dto.request;


public record FilterUserRequest(
        Boolean banned,   // true = только заблокированные, false = только активные, null = все
        String roleName,   // null = все роли
        String sortBy,
        String sortOrder
) {}
/*

{
  "banned": true,
  "roleName": null
}
*/
