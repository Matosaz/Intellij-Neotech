package com.backend.neotech.service;

import com.backend.neotech.model.Orcamento;
import com.backend.neotech.repository.OrcamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;

    @Autowired
    public OrcamentoService(OrcamentoRepository orcamentoRepository) {
        this.orcamentoRepository = orcamentoRepository;
    }

    // Método para salvar o orçamento
    public Orcamento salvarOrcamento(Orcamento orcamento) {
        return orcamentoRepository.save(orcamento);
    }

    // Método para listar todos os orçamentos
    public List<Orcamento> listarOrcamentos() {
        return orcamentoRepository.findAll();
    }

    // Método para listar orçamentos por usuário
    public List<Orcamento> getOrcamentosByUsuario(Long usuarioId) {
        return orcamentoRepository.findByUsuario_Id(usuarioId);
    }
    public List<Orcamento> getOrcamentosByCategoria(Long categoriaId) {
        return orcamentoRepository.findByCategoria_Id(categoriaId);
    }
    public Optional<Orcamento> buscarPorId(Long id) {
        return orcamentoRepository.findById(id);
    }

}
