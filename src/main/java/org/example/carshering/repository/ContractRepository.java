package org.example.carshering.repository;

import org.example.carshering.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract,Long> {
    @Query(
    """
    SELECT c FROM Contract c
        WHERE c.car.id = :carId
            AND c.state.name IN ('BOOKED', 'ACTIVE', 'PENDING')
            AND c.dataEnd > :startDate
            AND c.dataStart < :endDate
    """
    )
    List<Contract> findOverlappingContracts(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("carId") Long carId);

    @Query(
            """
            SELECT c FROM Contract c
                WHERE c.client.id = :userId
                    AND c.id = :contractId
            """
    )
    Optional<Contract> findByIdAndUserId(@Param("contractId") Long contractId, @Param("userId") Long userId);

    List<Contract> findByClientId(Long userId);
}
