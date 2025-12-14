package org.example.carshering.identity.domain.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.carshering.identity.domain.model.RoleModel;
import org.example.carshering.identity.domain.repository.RoleDomainRepository;
import org.example.carshering.identity.domain.valueobject.role.RoleId;
import org.example.carshering.identity.domain.valueobject.role.RoleName;
import org.example.carshering.identity.infrastructure.persistence.entity.Role;
import org.example.carshering.identity.infrastructure.persistence.mapper.RoleMapperForJpa;
import org.example.carshering.identity.infrastructure.persistence.repository.RoleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleDomainRepository {
    private final RoleRepository roleJpaRepository; // существующий
    private final RoleMapperForJpa roleMapper;

    @Override
    public Optional<RoleModel> findById(RoleId roleId) {
        return roleJpaRepository.findById(roleId.value())
                .map(roleMapper::toDomain);
    }

    @Override
    public Optional<RoleModel> findByName(RoleName roleName) {
        return roleJpaRepository.findByNameIgnoreCase(roleName.getValue())
                .map(roleMapper::toDomain);
    }

    @Override
    public List<RoleModel> findAll() {
        return roleJpaRepository.findAll().stream()
                .map(roleMapper::toDomain)
                .toList();
    }
}
