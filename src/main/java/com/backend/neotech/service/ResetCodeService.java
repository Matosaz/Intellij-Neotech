package com.backend.neotech.service;

import com.backend.neotech.model.ResetCode;
import com.backend.neotech.repository.ResetCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ResetCodeService {

    @Autowired
    private ResetCodeRepository resetCodeRepository;

    public void storeResetCode(String email, String code) {
        ResetCode resetCode = new ResetCode();
        resetCode.setEmail(email);
        resetCode.setCode(code);
        resetCode.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        resetCodeRepository.save(resetCode);
    }
    public boolean validateResetCode(String email, String code) {
        Optional<ResetCode> optionalResetCode = resetCodeRepository.findTopByEmailOrderByExpiresAtDesc(email);
        if (optionalResetCode.isEmpty()) return false;

        ResetCode stored = optionalResetCode.get();
        return stored.getCode().equals(code) && LocalDateTime.now().isBefore(stored.getExpiresAt());
    }
    public void removeResetCode(String email) {
        resetCodeRepository.deleteByEmail(email); // ou deleteAllByEmail se for mais de um
    }
}
