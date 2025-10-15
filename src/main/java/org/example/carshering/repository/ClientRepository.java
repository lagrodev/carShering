package org.example.carshering.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.carshering.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query(value = "Select count(c) > 0 from car_rental.client c where c.login = :login", nativeQuery = true)
    boolean existByLogin(@Param("login") String login);


    boolean existsByEmail(@NotBlank @Email String email);

    Optional<Client> getClientByLogin(String username);
}
