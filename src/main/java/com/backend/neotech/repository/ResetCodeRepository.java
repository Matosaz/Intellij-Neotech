package com.backend.neotech.repository;

import com.backend.neotech.model.ResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ResetCodeRepository extends JpaRepository<ResetCode, Long> {
    Optional<ResetCode> findByEmailAndCode(String email, String code);
    void deleteByEmail(String email);
}
