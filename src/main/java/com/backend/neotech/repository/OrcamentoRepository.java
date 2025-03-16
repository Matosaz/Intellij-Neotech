package com.backend.neotech.repository;

import com.backend.neotech.model.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

    // Método para encontrar orçamentos por ID de usuário
    List<Orcamento> findByUsuario_Id(Long usuarioId);
}
