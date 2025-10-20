package org.example.carshering.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client", schema = "car_rental")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = true, unique = true)
    private String phone;

    @Column(nullable = false, unique = true, name = "email" )
    private String email;
//todo сделать, чтобы email и т.п. не были unique через
// indexes = {
//           @Index(name = "uk_client_login_active", columnList = "login", where = "is_deleted = false"),
//           @Index(name = "uk_client_email_active", columnList = "email", where = "is_deleted = false")
//       }

    @Column(nullable = false)
    private String password;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false;

    @Column(name = "is_banned", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean banned =  false;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToMany(mappedBy = "client")
    private List<Contract> contracts = new ArrayList<>();

    @OneToOne(mappedBy = "client")
    private Document document;
}
