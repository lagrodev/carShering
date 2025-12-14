package org.example.carshering.identity.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.carshering.fleet.infrastructure.persistence.entity.Favorite;
import org.example.carshering.identity.domain.valueobject.role.RoleId;
import org.example.carshering.identity.domain.valueobject.user.Email;
import org.example.carshering.identity.domain.valueobject.user.Login;
import org.example.carshering.identity.domain.valueobject.user.Password;
import org.example.carshering.identity.domain.valueobject.user.Phone;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client", schema = "car_rental")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "favorites")
@Builder
@Setter
public class ClientJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "login"))
    })
    private Login login;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "phone"))
    })
    private Phone phone;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "email"))
    })
    private Email email;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "password", nullable = false))
    })
    private Password password;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false;

    @Column(name = "is_banned", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean banned = false;


    //@Column(name="email_verified", nullable = false, columnDefinition = "boolean default false")
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    //@Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
//    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;



    @OneToMany(mappedBy = "client")
    private List<Favorite> favorites = new ArrayList<>();


    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "role_id"))
    })
    private RoleId role;

}
