package org.example.carshering.rental.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.carshering.common.domain.valueobject.Money;
import org.example.carshering.domain.entity.Car;
import org.example.carshering.domain.entity.Client;
import org.example.carshering.rental.domain.valueobject.RentalPeriod;
import org.example.carshering.rental.domain.valueobject.RentalStateType;

@Entity
@Table(name = "contract", schema = "car_rental")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDate", column = @Column(name = "data_start", nullable = false)),
            @AttributeOverride(name = "endDate", column = @Column(name = "data_end", nullable = false)),
            @AttributeOverride(name = "durationMinutes", column = @Column(name = "duration_minutes", nullable = false))
    })
    private RentalPeriod period;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_cost")),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "currency"))
    })
    private Money totalCost;

    private String comment;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "car_id", nullable = false)
    private Long carId;


    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private RentalStateType state;
}
