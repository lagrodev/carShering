package org.example.carshering.identity.api.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to filter users")
public record FilterUserRequest(
        @Schema(description = "Filter by banned status: true = only banned, false = only active, null = all", example = "true")
        Boolean banned,   // true = только заблокированные, false = только активные, null = все
        @Schema(description = "Filter by role name, null = all roles", example = "USER")
        Long RoleId   // null = все роли

) {}
/*

{
  "banned": true,
  "roleName": null
}
*/
