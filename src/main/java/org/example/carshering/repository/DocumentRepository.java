package org.example.carshering.repository;

import org.example.carshering.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    boolean existsByClientId(Long clientId);
    Optional<Document> findByClientId(Long clientId);

    @Query(
            "SELECT d from Document d WHERE d.verified is false"
    )
    List<Document> findByVerifiedIsFalse();
}
