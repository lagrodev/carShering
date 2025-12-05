package org.example.carshering.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.carshering.domain.valueobject.Email;
import org.example.carshering.domain.valueobject.Login;
import org.example.carshering.domain.valueobject.Password;
import org.example.carshering.domain.valueobject.Phone;
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
@ToString(exclude = {"contracts", "favorites"})
@Builder
public class Client {
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

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToMany(mappedBy = "client")
    private List<Contract> contracts = new ArrayList<>();

    @OneToMany(mappedBy = "client")
    private List<Favorite> favorites = new ArrayList<>();

    /**
     * Verifies the client's email.
     * The client must be verified to rent a car.
     */
    public void verifyEmail() {
        if (this.emailVerified) {
            throw new IllegalStateException("Client email is already verified");
        }
        this.emailVerified = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Blocks the client, preventing them from renting cars.
     *
     * @throws IllegalStateException if client is already banned
     */
    public void ban() {
        if (this.banned) {
            throw new IllegalStateException("Client is already banned");
        }
        this.banned = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Unblocks the client, allowing them to rent cars again.
     *
     * @throws IllegalStateException if client is not banned
     */
    public void unban() {
        if (!this.banned) {
            throw new IllegalStateException("Client is not banned");
        }
        this.banned = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Soft deletes the client.
     */
    public void delete() {
        if (this.deleted) {
            throw new IllegalStateException("Client is already deleted");
        }
        this.deleted = true;
        this.updatedAt = Instant.now();
    }


    /**
     * Updates the client's contact information
     *
     * @param email new email (Necessarily)
     * @param phone new телефон (may be null)
     */
    public void updateContactInfo(Email email, Phone phone) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        this.email = email;
        this.phone = phone; // Телефон может быть null
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the client's personal information.
     *
     * @param firstName: client's first name
     * @param lastName:  client's last name (required)
     */
    public void updatePersonalInfo(String firstName, String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }
        this.firstName = firstName;
        this.lastName = lastName;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates client's password.
     * Password must be already encoded (BCrypt, etc.).
     *
     * @param encodedPassword new encoded password
     */
    public void updatePassword(Password encodedPassword) {
        if (encodedPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        if (!encodedPassword.isEncoded()) {
            throw new IllegalArgumentException("Password must be encoded before updating");
        }
        this.password = encodedPassword;
        this.updatedAt = Instant.now();
    }

    /**
     * Checks whether the client is active (not blocked or deleted).
     */
    public boolean isActive() {
        return !banned && !deleted;
    }

}
