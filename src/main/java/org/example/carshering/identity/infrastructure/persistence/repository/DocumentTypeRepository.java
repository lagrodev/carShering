package org.example.carshering.identity.infrastructure.persistence.repository;

import org.example.carshering.identity.infrastructure.persistence.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {}
