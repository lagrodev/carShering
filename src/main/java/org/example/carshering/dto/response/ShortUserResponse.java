package org.example.carshering.dto.response;

public record ShortUserResponse(
   Long id,
   String login,
   String email,
   String roleName,
   boolean banned
) {}
