package org.example.carshering.rental.infrastructure.persistence.repository;

import org.example.carshering.identity.infrastructure.persistence.entity.ClientJpaEntity;
import org.example.carshering.rental.domain.valueobject.RentalStateType;
import org.example.carshering.rental.infrastructure.persistence.entity.ContractJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<ContractJpaEntity, Long> {
    @Query("""
            SELECT c FROM ContractJpaEntity c
            WHERE c.carId = :carId
              AND (:contractId IS NULL OR c.id <> :contractId)
              AND c.state IN ('BOOKED', 'ACTIVE', 'PENDING', 'CONFIRMED')
              AND (
                (c.period.startDate < :endDate AND c.period.endDate > :startDate)
              )
            """)
    List<ContractJpaEntity> findOverlappingContracts(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("carId") Long carId,
            @Param("contractId") Long contractId
    );

    @Query("""
            SELECT c FROM ContractJpaEntity c
            WHERE c.carId = :carId
              AND c.state = 'ACTIVE'
              AND (
                (c.period.startDate < :endDate AND c.period.endDate > :startDate)
              )
            """)
    List<ContractJpaEntity> findByActiveContractsForCarInPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("carId") Long carId
    );


    @Query(
            """
                    SELECT c FROM ContractJpaEntity c
                        WHERE c.clientId = :userId
                            AND c.id = :contractId
                    """
    )
    Optional<ContractJpaEntity> findByIdAndUserId(@Param("contractId") Long contractId, @Param("userId") Long userId);

    Page<ContractJpaEntity> findByClientId(Long userId, Pageable pageable);

    @Query("""
            SELECT c FROM ContractJpaEntity c
            JOIN Car car ON car.id = c.carId
            JOIN CarModel model ON model.idModel = car.model.value
            JOIN Brand brand ON brand.id = model.brand.value
            LEFT JOIN CarClass cc ON cc.id = model.carClass.value
            WHERE (:status IS NULL OR c.state = :status)
              AND (:idUser IS NULL OR c.clientId = :idUser)
              AND (:idCar IS NULL OR c.carId = :idCar)
              AND (:brand IS NULL OR brand.name = :brand)
              AND (:bodyType IS NULL OR model.bodyType = :bodyType)
              AND (:carClass IS NULL OR cc.name = :carClass)
            """)
    Page<ContractJpaEntity> findAllByFilter(
            @Param("status") String status,
            @Param("idUser") Long idUser,
            @Param("idCar") Long idCar,
            @Param("brand") String brand,
            @Param("bodyType") String bodyType,
            @Param("carClass") String carClass,
            Pageable pageable
    );

    @Query("""
            SELECT c FROM ContractJpaEntity c 
            join ClientJpaEntity client on c.clientId = client.id
            WHERE client = :client AND UPPER(c.state) IN :activeStates
            """
    )
    List<ContractJpaEntity> findAllByClientAndActiveStates(@Param("client") ClientJpaEntity client, @Param("activeStates") Collection<String> activeStates);

    @Query(
            """
                    SELECT c FROM ContractJpaEntity c
                    JOIN c.state s
                    WHERE c.state = :confirmed
                      AND c.period.startDate < :localDate
                    """
    )
    List<ContractJpaEntity> findAllByStateNameAndStartDateBefore(RentalStateType confirmed, LocalDateTime localDate);

    @Query(
            """
                    SELECT c FROM ContractJpaEntity c
                    WHERE c.state = :completed
                      AND c.period.endDate < :localDateTime
                    """
    )
    List<ContractJpaEntity> findAllByStateNameAndDataEndBefore(RentalStateType completed, LocalDateTime localDateTime);


    @Query(
            """
                    SELECT c FROM ContractJpaEntity c
                    WHERE c.state = 'CONFIRMED'
                      AND c.period.startDate < :now
                    """
    )
    List<ContractJpaEntity> findContractJpaEntitiesByState_ConfirmedAndPeriod_StartDateBefore(LocalDateTime now);

    @Query(
            """
                    SELECT c FROM ContractJpaEntity c
                    WHERE c.state = :rentalStateType
                      AND c.period.endDate < :now
                    """
    )
    Collection<ContractJpaEntity> findContractJpaEntitiesByState_ActiveAndPeriod_EndDateBefore(String rentalStateType, LocalDateTime now);
}
