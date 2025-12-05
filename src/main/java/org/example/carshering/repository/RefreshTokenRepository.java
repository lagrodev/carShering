package org.example.carshering.repository;

import org.example.carshering.domain.entity.Client;
import org.example.carshering.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String token);


    List<RefreshToken> findAllByClient(Client user);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :revokedAt WHERE r.client = :client")
    void revokeAllByClient(@Param("client") Client client, @Param("revokedAt") Instant revokedAt);

    void deleteAllByRevokedAtBefore(Instant instant);
}
