package org.example.carshering.identity.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "verification_codes", schema = "car_rental")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String code;


    @Column(name = "created_at", nullable = false, updatable = false)
    //@Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private Instant createdAt;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "client_id", nullable = false))})
    private ClientId client;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private VerificationCodeType type;

    public enum VerificationCodeType {
        EMAIL_VERIFICATION,
        PASSWORD_RESET
    }

}