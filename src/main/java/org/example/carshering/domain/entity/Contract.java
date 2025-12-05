package org.example.carshering.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.carshering.domain.valueobject.Money;
import org.example.carshering.domain.valueobject.RentalPeriod;

@Entity
@Table(name = "contract", schema = "car_rental")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "client")
@Builder
public class Contract {
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

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;


    @ManyToOne
    @JoinColumn(name = "state_id", nullable = false)
    private RentalState state;
}
