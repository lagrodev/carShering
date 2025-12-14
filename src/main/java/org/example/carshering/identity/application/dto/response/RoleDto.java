package org.example.carshering.identity.application.dto.response;

import java.util.List;

public record RoleDto(
        Long id,
    String name,
    List<String> description
) {
}
