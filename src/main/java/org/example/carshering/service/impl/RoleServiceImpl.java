//package org.example.carshering.service.impl;
//
//import lombok.RequiredArgsConstructor;
//import org.example.carshering.identity.infrastructure.persistence.entity.Role;
//import org.example.carshering.identity.infrastructure.persistence.repository.RoleRepository;
//import org.example.carshering.service.interfaces.RoleService;
//import org.springframework.stereotype.Service;
//
//import org.example.carshering.common.exceptions.custom.RoleNotFoundException;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//public class RoleServiceImpl implements RoleService {
//    private final RoleRepository roleRepository;
//
//    @Override
//    public Optional<Role> getRole(Long id) {
//        return roleRepository.findById(id);
//    }
//
//    @Override
//    public Role getRoleByName(String name) {
//        return roleRepository.findByNameIgnoreCase(name)
//                .orElseThrow(() -> new RoleNotFoundException("Role not found with name: " + name));
//    }
//
//}
