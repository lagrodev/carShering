package org.example.carshering.dto.response;

import lombok.Builder;

@Builder
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
