package org.example.carshering.rental.infrastructure.persistence.repository;

import org.example.carshering.domain.entity.Client;
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
    JOIN car.model model
    WHERE (:status IS NULL OR c.state = :status)
      AND (:idUser IS NULL OR c.clientId = :idUser)
      AND (:idCar IS NULL OR c.carId = :idCar)
      AND (:brand IS NULL OR model.brand.name = :brand)
      AND (:bodyType IS NULL OR model.bodyType = :bodyType)
      AND (:carClass IS NULL OR model.carClass.name = :carClass)
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
            join Client client on c.clientId = client.id
            WHERE client = :client AND UPPER(c.state) IN :activeStates
            """
    )
    List<ContractJpaEntity> findAllByClientAndActiveStates(@Param("client") Client client, @Param("activeStates") Collection<String> activeStates);

    @Query(
            """
                    SELECT c FROM ContractJpaEntity c
                    JOIN c.state s
                    WHERE c.state = :confirmed
                      AND c.period.startDate < :localDate
                    """
    )
    List<ContractJpaEntity> findAllByStateNameAndStartDateBefore(String confirmed, LocalDateTime localDate);

    @Query(
            """
                    SELECT c FROM ContractJpaEntity c
                    WHERE c.state = :completed
                      AND c.period.endDate < :localDateTime
                    """
    )
    List<ContractJpaEntity> findAllByStateNameAndDataEndBefore(String completed, LocalDateTime localDateTime);
}
