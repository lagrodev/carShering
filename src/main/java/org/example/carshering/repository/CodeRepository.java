package org.example.carshering.repository;

import org.example.carshering.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByCode(String code);

    Optional<VerificationCode> findByCodeAndTypeIs(String code, VerificationCode.VerificationCodeType verificationCodeType);
}
