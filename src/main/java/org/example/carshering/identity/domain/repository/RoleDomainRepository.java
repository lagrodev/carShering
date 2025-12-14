package org.example.carshering.identity.domain.repository;

import org.example.carshering.identity.domain.model.RoleModel;
import org.example.carshering.identity.domain.valueobject.role.RoleId;
import org.example.carshering.identity.domain.valueobject.role.RoleName;

import java.util.List;
import java.util.Optional;

public interface RoleDomainRepository {
    Optional<RoleModel> findById(RoleId roleId);
    Optional<RoleModel> findByName(RoleName roleName);
    List<RoleModel> findAll();
}
