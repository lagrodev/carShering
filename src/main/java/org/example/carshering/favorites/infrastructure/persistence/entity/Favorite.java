package org.example.carshering.favorites.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "favorite", schema = "car_rental")
@Data
@ToString(exclude = "client")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "client_id"))
    })
    private ClientId client;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "car_id"))
    })
    private CarId car;

    @CreationTimestamp
    private Instant createdAt;

}
