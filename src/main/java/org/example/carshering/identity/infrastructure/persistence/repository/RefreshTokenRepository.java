package org.example.carshering.identity.infrastructure.persistence.repository;

import org.example.carshering.identity.infrastructure.persistence.entity.ClientJpaEntity;
import org.example.carshering.identity.infrastructure.persistence.entity.RefreshToken;
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


    @Query(
            """
                    SELECT r FROM RefreshToken r
                    WHERE r.client = :client AND r.revoked = false AND r.expiryDate > :now
                    """
    )
    List<RefreshToken> findAllByClient(ClientJpaEntity user);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :revokedAt WHERE r.client = :client")
    void revokeAllByClient(@Param("client") ClientJpaEntity client, @Param("revokedAt") Instant revokedAt);

    void deleteAllByRevokedAtBefore(Instant instant);
}
