package org.example.carshering.identity.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor

@Table(
        name = "refresh_tokens",
        indexes = @Index(columnList = "client_id")
        , schema = "car_rental"
)
public class RefreshToken {

    @Id()
    @GeneratedValue(generator = "refresh_token_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "refresh_token_seq", sequenceName = "refresh_token_sequence", allocationSize = 1)
    private Long id;


    @Column(name = "token_hash", nullable = false, unique = true, length = 60)
    private String tokenHash;


    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "client_id"))
    })
    private ClientId client;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column
    private Instant revokedAt;


    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }
    // todo: hard deleted token if revoked At > 30 days
    // TODO: maybe soft delete with a boolean flag 'revoked', and notify user to email
}
