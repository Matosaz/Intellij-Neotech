package com.backend.neotech.service;
import com.backend.neotech.model.Orcamento;
import com.backend.neotech.repository.OrcamentoRepository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.neotech.model.Categoria;
import com.backend.neotech.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final OrcamentoRepository orcamentoRepository;

    @Autowired
    public CategoriaService(CategoriaRepository categoriaRepository, OrcamentoRepository orcamentoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.orcamentoRepository = orcamentoRepository;
    }

    public Categoria salvarCategoria(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public Optional<Categoria> buscarPorId(Long id) {
        return categoriaRepository.findById(id);
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    @Transactional
    public Categoria atualizarCategoria(Long id, Categoria categoriaAtualizada) {
        return categoriaRepository.findById(id).map(categoria -> {
            // Guarda o status atual antes da atualização
            String statusAnterior = categoria.getCodStatus();

            // Atualiza os campos da categoria
            categoria.setNome(categoriaAtualizada.getNome());
            categoria.setDescricao(categoriaAtualizada.getDescricao());
            categoria.setPrecoPorKg(categoriaAtualizada.getPrecoPorKg());
            categoria.setCodStatus(categoriaAtualizada.getCodStatus());

            // Salva a categoria atualizada
            Categoria categoriaSalva = categoriaRepository.save(categoria);

            // Verifica se a categoria está sendo inativada (mudando de ATIVO para INATIVO)
            if ("ATIVO".equals(statusAnterior) && "INATIVO".equals(categoriaAtualizada.getCodStatus())) {
                // Inativa todos os orçamentos relacionados a esta categoria
                inativarOrcamentosRelacionados(id);
            }

            return categoriaSalva;
        }).orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
    }

    private void inativarOrcamentosRelacionados(Long categoriaId) {
        // Busca todos os orçamentos que têm esta categoria
        List<Orcamento> orcamentos = orcamentoRepository.findByCategoriasId(categoriaId);

        // Atualiza o status de cada orçamento para INATIVO
        orcamentos.forEach(orcamento -> {
            orcamento.setCodStatus("INATIVO");
            orcamentoRepository.save(orcamento);
        });
    }

    public void deleteCategoria(Long id) {
        categoriaRepository.deleteById(id);
    }

}
