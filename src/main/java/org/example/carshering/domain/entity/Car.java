package org.example.carshering.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.carshering.common.domain.valueobject.Money;
import org.example.carshering.domain.valueobject.GosNumber;
import org.example.carshering.domain.valueobject.Vin;
import org.example.carshering.domain.valueobject.Year;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "car", schema = "car_rental")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "gos_number", nullable = false, unique = true))
    })
    private GosNumber gosNumber;


    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "vin", nullable = false, unique = true))
    })
    private Vin vin;


    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "rent")),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "currency"))
    })
    private Money dailyRate; // rental price per day


    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "year_of_issue", nullable = false))
    })
    private Year yearOfIssue;

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    private CarModel model;

    @ManyToOne
    @JoinColumn(name = "state_id", nullable = false)
    private CarState state;

//    @OneToMany(mappedBy = "car")
//    private List<Contract> contracts = new ArrayList<>();

    @OneToMany(mappedBy = "car")
    private List<Favorite> favorites = new ArrayList<>();


    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    private List<Image> images = new ArrayList<>();
    // TODO: @CreateDate @UpdateDate
}