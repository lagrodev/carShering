package org.example.carshering.identity.infrastructure.persistence.repository;

import org.example.carshering.identity.infrastructure.persistence.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByCode(String code);

    Optional<VerificationCode> findByCodeAndTypeIs(String code, VerificationCode.VerificationCodeType verificationCodeType);
}
