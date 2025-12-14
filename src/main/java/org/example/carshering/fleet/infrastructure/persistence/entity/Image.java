package org.example.carshering.fleet.infrastructure.persistence.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.fleet.domain.valueobject.FileName;
import org.example.carshering.fleet.domain.valueobject.ImageUrl;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "images", schema = "car_rental")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "fileName", nullable = false))
    })
    private FileName fileName;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "url", nullable = false))
    })
    private ImageUrl url;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "car_id", nullable = false))
    })
    private CarId car;
    //    @ManyToOne
//    @JoinColumn(name = "car_id")
}
