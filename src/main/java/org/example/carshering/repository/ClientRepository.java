package org.example.carshering.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.carshering.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query(value = "Select count(c) > 0 from car_rental.client c where c.login = :login", nativeQuery = true)
    boolean existByLogin(@Param("login") String login);

    boolean existsByLoginAndDeletedFalse(String login);
    boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByEmail(@NotBlank @Email String email);

    Optional<Client> getClientByLogin(String username);

    @Query(value = """
    SELECT * FROM car_rental.client c
    WHERE (:banned IS NULL OR c.is_banned = :banned)
    AND (:roleName IS NULL OR c.role_id IN (
        SELECT r.id FROM car_rental.role r WHERE r.name = :roleName
    ))
    ORDER BY
        CASE WHEN :sortBy = 'id' THEN c.id END,
        CASE WHEN :sortBy = 'email' THEN c.email END,
        CASE WHEN :sortBy = 'login' THEN c.login END
    """,
            nativeQuery = true)
    List<Client> findByFilter(
            @Param("banned") Boolean banned,
            @Param("roleName") String roleName,
            @Param("sortBy") String sortBy
    );

}
