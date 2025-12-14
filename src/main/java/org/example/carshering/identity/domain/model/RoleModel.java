package org.example.carshering.identity.domain.model;

import lombok.Getter;
import org.example.carshering.identity.domain.valueobject.role.Permission;
import org.example.carshering.identity.domain.valueobject.role.RoleId;
import org.example.carshering.identity.domain.valueobject.role.RoleName;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
public class RoleModel {
    private final RoleId roleId;
    private RoleName name;
    private Set<Permission> permissions;
    private final boolean systemRole;


    private RoleModel(RoleId roleId, RoleName name, Set<Permission> permissions, boolean systemRole) {
        this.roleId = roleId;
        this.name = name;
        this.permissions = new HashSet<>(permissions);
        this.systemRole = systemRole;
    }

    public static RoleModel reconstruct(RoleId roleId, RoleName name,
                                   Set<Permission> permissions, boolean systemRole) {
        return new RoleModel(roleId, name, permissions, systemRole);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

}
