package org.example.carshering.dto.response;

public record UserResponse(
   Long id,
   String firstName,
   String lastName,
   String login,
   String phone,
   String email
) {}
