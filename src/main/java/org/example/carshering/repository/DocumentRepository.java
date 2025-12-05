package org.example.carshering.repository;

import org.example.carshering.domain.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    // todo возможно тогда эти методы нужны
    boolean existsByClientId(Long clientId);
    Optional<Document> findByClientId(Long clientId);

    @Query(
            "SELECT d from Document d WHERE d.verified is false"
    )
    Page<Document> findByVerifiedIsFalse(Pageable pageable);

    // todo возможность вернуть удаленные документы для админа??
    Optional<Document> findByClientIdAndDeletedFalse(Long clientId);
    boolean existsByClientIdAndDeletedFalse(Long clientId);

    @Query("""
    SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END
    FROM Client c
    WHERE c.banned = true
      AND EXISTS (
        SELECT 1 FROM Document d
        WHERE d.client = c
          AND d.series = :series
          AND d.number = :number
      )
""")
    boolean existsByDocumentSeriesAndNumberAndClientBannedTrue(@Param("series") String series,
                                                               @Param("number") String number);

    boolean existsBySeriesAndNumber(String series, String number);
}
