package com.backend.neotech.repository;
import com.backend.neotech.model.Orcamento;
import com.backend.neotech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

}
