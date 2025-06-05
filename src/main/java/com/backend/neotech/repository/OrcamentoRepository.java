package com.backend.neotech.repository;

import com.backend.neotech.model.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

    // Método para encontrar orçamentos por ID de usuário
    List<Orcamento> findByUsuario_Id(Long usuarioId);
    List<Orcamento> findByCategorias_Id(Long categoriaId);
    // Busca orçamentos de um usuário carregando categorias (evita categorias vazias no JSON)
    @Query("SELECT DISTINCT o FROM Orcamento o LEFT JOIN FETCH o.categorias WHERE o.usuario.id = :usuarioId")
    List<Orcamento> findByUsuarioIdWithCategorias(@Param("usuarioId") Long usuarioId);

    // Busca orçamentos por categoria, carregando categorias também
    @Query("SELECT DISTINCT o FROM Orcamento o LEFT JOIN FETCH o.categorias c WHERE c.id = :categoriaId")
    List<Orcamento> findByCategoriaIdWithCategorias(@Param("categoriaId") Long categoriaId);

    // Busca todos os orçamentos com categorias carregadas
    @Query("SELECT DISTINCT o FROM Orcamento o LEFT JOIN FETCH o.categorias")
    List<Orcamento> findAllWithCategorias();

    List<Orcamento> findByCategoriasId(Long categoriaId);
}
