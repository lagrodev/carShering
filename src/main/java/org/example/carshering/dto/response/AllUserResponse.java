package org.example.carshering.dto.response;

public record AllUserResponse(
   Long id,
   String firstName,
   String lastName,
   String login,
   String phone,
   String email,
   String roleName,
   boolean banned
) {}
