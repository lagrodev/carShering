package org.example.carshering.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.carshering.domain.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query(value = "Select count(c) > 0 from Client c where c.login = :login")
    boolean existByLogin(@Param("login") String login);

    boolean existsByLoginAndDeletedFalse(String login);

    boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByEmail(@NotBlank @Email String email);


    @Query("""
            SELECT c FROM Client c 
            WHERE (c.banned = false )
            AND ( c.deleted = false)
            AND (c.login = :username)
            """
    )
    Optional<Client> getClientByLogin(String username);

    @Query("SELECT c FROM Client c " +
            "WHERE (:banned IS NULL OR c.banned = :banned) " +
            "AND (:roleName IS NULL OR c.role.name = :roleName)")
    Page<Client> findByFilter(
            @Param("banned") Boolean banned,
            @Param("roleName") String roleName,
            Pageable pageable
    );


    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Client c WHERE c.phone = :phone and c.deleted = false and c.id != :id
            """)
    boolean existsByPhoneAndIdNot(String phone, Long id);

    Optional<Client> findByEmailAndDeletedFalse(String email);
}
