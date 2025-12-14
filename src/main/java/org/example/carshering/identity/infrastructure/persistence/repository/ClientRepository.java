package org.example.carshering.identity.infrastructure.persistence.repository;

import com.google.common.io.Files;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.carshering.identity.domain.valueobject.role.RoleId;
import org.example.carshering.identity.infrastructure.persistence.entity.ClientJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<ClientJpaEntity, Long> {

    @Query(value = "Select count(c) > 0 from ClientJpaEntity c where c.login.value = :login")
    boolean existByLogin(@Param("login") String login);

    @Query(
            """
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ClientJpaEntity c WHERE c.login.value = :login and c.deleted = false
            """
    )
    boolean existsByLoginAndDeletedFalse(String login);

    @Query(
            """
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ClientJpaEntity c WHERE c.email.value = :email and c.deleted = false
            """
    )
    boolean existsByEmailAndDeletedFalse(String email);

    @Query(
            """
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ClientJpaEntity c WHERE c.email.value = :email
            """
    )
    boolean existsByEmail(@NotBlank @Email String email);


    @Query("""
            SELECT c FROM ClientJpaEntity c
            WHERE (c.banned = false )
            AND ( c.deleted = false)
            AND (c.login.value = :username)
            """
    )
    Optional<ClientJpaEntity> getClientByLogin(String username);

    @Query("SELECT c FROM ClientJpaEntity c " +
            "WHERE (:banned IS NULL OR c.banned = :banned) " +
            "AND (:roleId IS NULL OR c.role = :roleId)")
    Page<ClientJpaEntity> findByFilter(
            @Param("banned") Boolean banned,
            @Param("roleName") RoleId roleId,
            Pageable pageable
    );


    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ClientJpaEntity c WHERE c.phone.value = :phone and c.deleted = false and c.id != :id
            """)
    boolean existsByPhoneAndIdNot(String phone, Long id);

    @Query(
            """
            SELECT c FROM ClientJpaEntity c WHERE c.email.value = :email AND c.deleted = false
            """
    )
    Optional<ClientJpaEntity> findByEmailAndDeletedFalse(String email);

    @Query(
            """
            SELECT distinct c FROM ClientJpaEntity c WHERE c.role = :roleId AND c.deleted = false
            """
    )
    List<ClientJpaEntity> findByRoleAndDeletedFalse(RoleId roleId);

    @Query("""
            SELECT c FROM ClientJpaEntity c
            LEFT JOIN FETCH DocumentJpaEntity d ON c.id = d.clientId
            WHERE d.verified = false
            AND c.deleted = false
            """)
    Page<ClientJpaEntity> findClientsWithUnverifiedDocuments(Pageable pageable);

    @Query(
            """
            SELECT distinct c FROM ClientJpaEntity c
            LEFT JOIN FETCH DocumentJpaEntity d ON c.id = d.clientId
            WHERE c.id = :value
            """
    )
    Optional<ClientJpaEntity> findByIdWithDocument(Long value);

    @Query(
            """
            select c from ClientJpaEntity c where c.login.value = :value and c.deleted = false
"""
    )
    Optional<ClientJpaEntity> findByLoginAndDeletedFalse(String value);

    @Query(
            """
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ClientJpaEntity c WHERE c.phone.value = :value AND c.id != :id AND c.deleted = false
            """
    )
    boolean existsByPhoneAndDeletedFalse(Long id, String value);
}
