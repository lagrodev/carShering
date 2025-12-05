package org.example.carshering.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role", schema = "car_rental")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
