package org.example.carshering.identity.infrastructure.persistence.repository;

import org.example.carshering.identity.infrastructure.persistence.entity.DocumentJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentJpaEntity, Long> {
    // todo возможно тогда эти методы нужны
    boolean existsByClientId(Long clientId);

    @Query(
            "SELECT d from DocumentJpaEntity d WHERE d.clientId = :clientId and d.deleted is false"
    )
    Optional<DocumentJpaEntity> findByClientId(Long clientId);



    @Query(
            "SELECT d from DocumentJpaEntity d WHERE d.verified is false"
    )
    Page<DocumentJpaEntity> findByVerifiedIsFalse(Pageable pageable);

    // todo возможность вернуть удаленные документы для админа??
    Optional<DocumentJpaEntity> findByClientIdAndDeletedFalse(Long clientId);

    boolean existsByClientIdAndDeletedFalse(Long clientId);

    @Query("""
                SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END
                FROM ClientJpaEntity c
                WHERE c.banned = true
                  AND EXISTS (
                    SELECT 1 FROM DocumentJpaEntity d
                    WHERE d.clientId = c.id
                      AND d.series.value = :series
                      AND d.number.value = :number
                  )
            """)
    boolean existsByDocumentSeriesAndNumberAndClientBannedTrue(@Param("series") String series,
                                                               @Param("number") String number);

    @Query(
            """
                        SELECT CASE WHEN COUNT(d) > 0 THEN TRUE ELSE FALSE END
                        FROM DocumentJpaEntity d
                        WHERE d.series.value = :series
                          AND d.number.value = :number
                    """
    )
    boolean existsBySeriesAndNumber(String series, String number);

    @Query(
            """
        select d from DocumentJpaEntity d
        where d.clientId in :clientIds
"""
    )
    List<DocumentJpaEntity> findByClientIdIn(List<Long> clientIds);

    @Query(
            """
        SELECT d FROM DocumentJpaEntity d
        WHERE (:onlyUnverified = false OR d.verified = false)
"""
    )
    Page<DocumentJpaEntity> findAllForFilter(boolean onlyUnverified, Pageable pageable);
}
