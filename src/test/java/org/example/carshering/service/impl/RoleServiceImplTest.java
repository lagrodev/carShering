package org.example.carshering.service.impl;

import org.example.carshering.identity.infrastructure.persistence.entity.Role;
import org.example.carshering.common.exceptions.custom.RoleNotFoundException;
import org.example.carshering.identity.infrastructure.persistence.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RoleServiceImplTest {

    @Mock
    private RoleRepository documentTypeRepository;

    @InjectMocks
    private RoleServiceImpl serviceUnderTest;

    @Test
    @DisplayName("givenExistingRoles_whenGetRoleByName_thenReturnRole")
    public void givenExistingRoles_whenGetRoleByName_thenReturnRole() {
        // given
        String name = "ROLE";
        Role role = Role.builder().id(1L)
                .name(name)
                .build();
        given(documentTypeRepository.findByNameIgnoreCase(name)).willReturn(java.util.Optional.ofNullable(role));

        Role actual = serviceUnderTest.getRoleByName(name);

        // then
        assert actual.equals(role);
        verify(documentTypeRepository).findByNameIgnoreCase(name);
    }

    @Test
    @DisplayName("givenNonExistingRoles_whenGetRoleByName_thenThrowRoleNotFoundException")
    public void givenNonExistingRoles_whenGetRoleByName_thenThrowRoleNotFoundException() {
        // given
        String name = "ROLE";
        given(documentTypeRepository.findByNameIgnoreCase(name)).willReturn(java.util.Optional.empty());

        // when - then
        assertThrows(RoleNotFoundException.class,
                () -> serviceUnderTest.getRoleByName(name),
                "Role not found with name: " + name
        );

        verify(documentTypeRepository).findByNameIgnoreCase(name);
    }

    @Test
    @DisplayName("givenExistingRoleId_whenGetRole_thenReturnRole")
    public void givenExistingRoleId_whenGetRole_thenReturnRole() {
        // given
        Long id = 1L;
        Role role = Role.builder().id(id)
                .name("ROLE")
                .build();
        given(documentTypeRepository.findById(id)).willReturn(java.util.Optional.ofNullable(role));

        Optional<Role> actual = serviceUnderTest.getRole(id);

        // then
        assert actual.isPresent() && actual.get().equals(role);
        verify(documentTypeRepository).findById(id);
    }

    @Test
    @DisplayName("givenNonExistingRoleId_whenGetRole_thenReturnEmptyOptional")
    public void givenNonExistingRoleId_whenGetRole_thenReturnEmptyOptional() {
        // given
        Long id = 1L;
        given(documentTypeRepository.findById(id)).willReturn(java.util.Optional.empty());

        Optional<Role> actual = serviceUnderTest.getRole(id);

        // then
        assert actual.isEmpty();
        verify(documentTypeRepository).findById(id);
    }


}
