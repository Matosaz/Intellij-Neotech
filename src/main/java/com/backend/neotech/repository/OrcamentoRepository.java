package com.backend.neotech.repository;

import com.backend.neotech.model.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

    // Busca os orçamentos associados a um usuário específico
    List<Orcamento> findByUsuario_Id(Long usuarioId);

    // Consulta todos os orçamentos
    List<Orcamento> findAll();
}
