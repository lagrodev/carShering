package org.example.carshering.repository.impl;

import org.example.carshering.entity.Role;
import org.example.carshering.repository.AbstractRepositoryTest;
import org.example.carshering.repository.RentalStateRepository;
import org.example.carshering.repository.RoleRepository;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
@Import(DataUtils.class)
@ActiveProfiles("test")
public class RoleRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DataUtils dataUtils;

    @BeforeEach
    public void setUp() {
        roleRepository.deleteAll();
    }


    @Test
    @DisplayName("Test save role functionality")
    public void givenRoleObject_whenSave_thenRoleIsCreated() {
        // given
        Role roleToSave = dataUtils.getRoleTransient();

        // when
        Role savedRole = roleRepository.save(roleToSave);

        // then
        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getName()).isEqualTo(roleToSave.getName());
    }

    @Test
    @DisplayName("Test update role functionality")
    public void givenRoleToUpdate_whenSave_thenRoleIsChanged() {
        // given
        Role roleToSave = dataUtils.getRoleTransient();
        Role savedRole = roleRepository.save(roleToSave);

        String updatedName = "UpdatedRole";

        // when
        savedRole.setName(updatedName);
        Role updatedRole = roleRepository.save(savedRole);

        // then
        assertThat(updatedRole).isNotNull();
        assertThat(updatedRole.getName()).isEqualTo(updatedName);
    }

    @Test
    @DisplayName("Test get role by id functionality")
    public void givenRoleCreated_whenFindById_thenRoleIsReturned() {
        // given
        Role roleToSave = dataUtils.getRoleTransient();
        Role savedRole = roleRepository.save(roleToSave);

        // when
        Role obtainedRole = roleRepository.findById(savedRole.getId()).orElse(null);

        // then
        assertThat(obtainedRole).isNotNull();
        assertThat(obtainedRole.getName()).isEqualTo(roleToSave.getName());
    }

    @Test
    @DisplayName("Test role not found by id functionality")
    public void givenRoleIsNotCreated_whenFindById_thenOptionalIsEmpty() {
        // when
        Role obtainedRole = roleRepository.findById(1L).orElse(null);

        // then
        assertThat(obtainedRole).isNull();
    }

    @Test
    @DisplayName("Test find role by name returns role when exists")
    public void givenRoleExists_whenFindByNameIgnoreCase_thenRoleIsReturned() {
        // given
        Role roleToSave = dataUtils.getRoleTransient();
        roleRepository.save(roleToSave);

        // when
        Optional<Role> foundRole = roleRepository.findByNameIgnoreCase(roleToSave.getName());

        // then
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo(roleToSave.getName());
    }

    @Test
    @DisplayName("Test find role by name returns empty when role does not exist")
    public void givenRoleDoesNotExist_whenFindByNameIgnoreCase_thenOptionalIsEmpty() {
        // when
        Optional<Role> foundRole = roleRepository.findByNameIgnoreCase("NonExistentRole");

        // then
        assertThat(foundRole).isEmpty();
    }

    @Test
    @DisplayName("Test find role by name is case-insensitive")
    public void givenRoleExistsWithDifferentCase_whenFindByNameIgnoreCase_thenRoleIsReturned() {
        // given
        Role role = Role.builder().name("ADMIN").build();
        roleRepository.save(role);

        // when
        Optional<Role> found1 = roleRepository.findByNameIgnoreCase("admin");
        Optional<Role> found2 = roleRepository.findByNameIgnoreCase("Admin");
        Optional<Role> found3 = roleRepository.findByNameIgnoreCase("ADMIN");

        // then
        assertThat(found1).isPresent().map(Role::getName).hasValue("ADMIN");
        assertThat(found2).isPresent().map(Role::getName).hasValue("ADMIN");
        assertThat(found3).isPresent().map(Role::getName).hasValue("ADMIN");
    }

    @Test
    @DisplayName("Test cannot save role with same name in different case due to unique constraint")
    void givenRoleExists_whenSaveSameNameDifferentCase_thenThrowsDataIntegrityViolation() {
        // given
        roleRepository.save(Role.builder().name("USER").build());

        // when & then
        Role duplicate = Role.builder().name("user").build();
        assertThatThrownBy(() -> roleRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Test get all roles functionality")
    public void givenThreeRolesAreStored_whenFindAll_thenAllRolesAreReturned() {
        // given
        Role role1 = dataUtils.getRoleTransient();
        Role role2 = Role.builder().name("AnotherRole").build();
        Role role3 = Role.builder().name("ThirdRole").build();

        roleRepository.saveAll(List.of(role1, role2, role3));

        // when
        List<Role> obtainedRoles = roleRepository.findAll();

        // then
        assertThat(CollectionUtils.isEmpty(obtainedRoles)).isFalse();
        assertThat(obtainedRoles).hasSize(3);
        assertThat(obtainedRoles)
                .extracting(Role::getName)
                .containsExactlyInAnyOrder(role1.getName(), role2.getName(), role3.getName());
    }

    @Test
    @DisplayName("Test get all roles when no roles stored functionality")
    public void givenNoRolesAreStored_whenFindAll_thenEmptyListIsReturned() {
        // when
        List<Role> obtainedRoles = roleRepository.findAll();

        // then
        assertThat(CollectionUtils.isEmpty(obtainedRoles)).isTrue();
    }

    @Test
    @DisplayName("Test delete role by id functionality")
    public void givenRoleIsSaved_whenDeleteById_thenRoleIsRemoved() {
        // given
        Role roleToSave = dataUtils.getRoleTransient();
        Role savedRole = roleRepository.save(roleToSave);

        // when
        roleRepository.deleteById(savedRole.getId());

        // then
        Role obtainedRole = roleRepository.findById(savedRole.getId()).orElse(null);
        assertThat(obtainedRole).isNull();
    }

    @Test
    @DisplayName("Test save role with duplicate name throws exception due to unique constraint")
    public void givenRoleWithDuplicateName_whenSaved_thenThrowsDataIntegrityViolationException() {
        // given
        String roleName = "UniqueRole";
        Role role1 = Role.builder().name(roleName).build();
        roleRepository.save(role1);

        Role role2 = Role.builder().name(roleName).build();

        // when & then
        assertThatThrownBy(() -> roleRepository.saveAndFlush(role2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("unique");
    }

    /**
     * Проверяет, что метод {@link RentalStateRepository#findByNameIgnoreCase(String)} устойчив
     * к попыткам SQL-инъекции: злонамеренные входные строки интерпретируются как обычные
     * строковые значения, а не исполняемый SQL-код.
     * <p>
     * Тест сохраняет легитимный бренд ("MODEL"), затем пытается найти модуль с именами,
     * содержащими типичные паттерны SQL-инъекций. Ожидается, что результат всегда пустой.
     * После этого проверяется, что легитимный бренд по-прежнему находится при корректном запросе.
     * <p>
     * Цель: подтвердить защиту на уровне репозитория без полагания только на Spring Data JPA.
     */
    @Test
    @DisplayName("findByNameIgnoreCase is safe against SQL injection attempts")
    void findByNameIgnoreCase_sqlInjectionAttempt_returnsEmpty() {
        // given
        Role legitimateBrand = new Role();
        legitimateBrand.setName("MODEL");
        roleRepository.save(legitimateBrand);

        // Популярные паттерны SQL-инъекций
        String[] maliciousInputs = {
                "' OR '1'='1' --",
                "'; DROP TABLE model; --",
                "' UNION SELECT null, version() --",
                "\" OR \"\"=\"",
                "admin'--",
                "'; SELECT * FROM users; --"
        };

        for (String input : maliciousInputs) {
            // when
            Optional<Role> result = roleRepository.findByNameIgnoreCase(input);

            // then
            assertThat(result).isEmpty(); // Никакой бренд с таким именем не существует
        }

        // Убедимся, что легитимный бренд всё ещё находится
        Optional<Role> found = roleRepository.findByNameIgnoreCase("model");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("MODEL");
    }

}