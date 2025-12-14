package org.example.carshering.identity.infrastructure.persistence.mapper;

import lombok.RequiredArgsConstructor;
import org.example.carshering.identity.domain.model.RoleModel;
import org.example.carshering.identity.domain.valueobject.role.Permission;
import org.example.carshering.identity.domain.valueobject.role.RoleId;
import org.example.carshering.identity.domain.valueobject.role.RoleName;
import org.example.carshering.identity.infrastructure.persistence.entity.Role;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleMapperForJpa {

    public RoleModel toDomain(Role entity) {
        if (entity == null) return null;

        Set<Permission> permissions = entity.getPermissions().stream()
                .map(p -> p.getName()) // Permission enum
                .collect(Collectors.toSet());

        return RoleModel.reconstruct(
                new RoleId(entity.getId()),
                RoleName.of(entity.getName()),
                permissions,
                true // systemRole - можно добавить поле в БД
        );
    }
}
