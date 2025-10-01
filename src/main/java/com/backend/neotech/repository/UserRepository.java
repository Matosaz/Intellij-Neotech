package com.backend.neotech.repository;
import com.backend.neotech.model.Orcamento;
import com.backend.neotech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u.id as id, u.nome as nome, u.email as email, SUM(o.pontos) as totalPontos " +
            "FROM User u LEFT JOIN Orcamento o ON o.usuario.id = u.id " +
            "GROUP BY u.id, u.nome, u.email")
    List<UserSummary> findAllUsersWithTotalPoints();


    @Query("SELECT u.avatar FROM User u WHERE u.id = :userId")
    Optional<byte[]> findAvatarByUserId(Long userId);
}
