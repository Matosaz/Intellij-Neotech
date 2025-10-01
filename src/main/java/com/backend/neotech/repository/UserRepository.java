package com.backend.neotech.repository;

import com.backend.neotech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.backend.neotech.repository.UserSummary;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u.id as id, u.nome as nome, u.email as email, SUM(o.pontos) as totalPontos " +
            "FROM User u LEFT JOIN Orcamento o ON o.usuario.id = u.id AND o.codStatus = 'CONCLUIDA'" +
            "GROUP BY u.id, u.nome, u.email")
    List<UserSummary> findAllUsersWithTotalPoints();


    @Query("SELECT u.avatar FROM User u WHERE u.id = :userId")
    Optional<byte[]> findAvatarByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(o.pontos), 0) FROM Orcamento o WHERE o.usuario.id = :userId AND o.codStatus = 'CONCLUIDA'")
    Integer findTotalPointsByUserId(Long userId);


    @Query("SELECT u.id FROM User u " +
            "LEFT JOIN Orcamento o ON o.usuario.id = u.id AND o.codStatus = 'CONCLUIDA' " +
            "GROUP BY u.id " +
            "ORDER BY SUM(o.pontos) DESC")
    List<Long> findTopUserId();


}
