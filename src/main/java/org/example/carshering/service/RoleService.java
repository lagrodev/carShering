package org.example.carshering.service;

import org.example.carshering.entity.Role;

import java.util.Optional;

public interface RoleService {
    Optional<Role> getRole(Long id);
    Role getRoleByName(String name);
}
