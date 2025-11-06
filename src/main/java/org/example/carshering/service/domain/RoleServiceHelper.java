package org.example.carshering.service.domain;

import lombok.RequiredArgsConstructor;
import org.example.carshering.entity.Role;
import org.example.carshering.exceptions.custom.RoleNotFoundException;
import org.example.carshering.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceHelper {
    public Role getRoleByName(String name) {
        return roleRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with name: " + name));
    }
    private final RoleRepository roleRepository;

}
