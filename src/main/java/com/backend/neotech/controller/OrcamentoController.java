package com.backend.neotech.controller;

import com.backend.neotech.model.Orcamento;
import com.backend.neotech.model.User;
import com.backend.neotech.repository.UserRepository;
import com.backend.neotech.service.OrcamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/orcamentos")
public class OrcamentoController {

    @Autowired
    private UserRepository userRepository;  // Injeção do repositório UserRepository

    private final OrcamentoService orcamentoService;

    // Injeção de dependência do serviço OrcamentoService
    @Autowired
    public OrcamentoController(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }

    // Endpoint para criar um novo orçamento
    @PostMapping
    public ResponseEntity<String> criarOrcamento(@RequestBody Orcamento orcamento) {
        // Validações básicas
        if (orcamento.getMetodoContato() == null || orcamento.getMetodoContato().isEmpty()) {
            return ResponseEntity.badRequest().body("Método de contato é obrigatório.");
        }
        if (orcamento.getHoraColeta() == null) {
            return ResponseEntity.badRequest().body("Hora da coleta é obrigatória.");
        }
        if (orcamento.getDataColeta() == null) {
            return ResponseEntity.badRequest().body("Data da coleta é obrigatória.");
        }
        if (orcamento.getAceitaContato() == null) {
            return ResponseEntity.badRequest().body("Aceite de contato é obrigatório.");
        }

        // Verifica se o usuário existe
        if (orcamento.getUsuario() == null || orcamento.getUsuario().getId() == null) {
            return ResponseEntity.badRequest().body("Usuário inválido ou não fornecido.");
        }

        // Verifica se o usuário existe no banco de dados
        Optional<User> usuarioOptional = userRepository.findById(orcamento.getUsuario().getId());
        if (!usuarioOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Usuário não encontrado.");
        }

        try {
            // Salva o orçamento utilizando o serviço
            Orcamento savedOrcamento = orcamentoService.salvarOrcamento(orcamento);

            // Criação de URI para o novo orçamento
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(savedOrcamento.getId())
                    .toUri();

            return ResponseEntity.created(location).body("Orçamento criado com sucesso.");
        } catch (Exception e) {
            e.printStackTrace(); // 👈 loga no console
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao salvar o orçamento.");
        }

    }

    // Endpoint para listar todos os orçamentos
    @GetMapping
    public ResponseEntity<List<Orcamento>> getAllOrcamentos() {
        List<Orcamento> orcamentos = orcamentoService.listarOrcamentos();
        return ResponseEntity.ok(orcamentos);
    }

    // Endpoint para listar orçamentos por usuário
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Orcamento>> getOrcamentosByUsuario(@PathVariable Long usuarioId) {
        // Verifica se o usuário com o id fornecido existe
        Optional<User> usuarioOptional = userRepository.findById(usuarioId);
        if (!usuarioOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.emptyList());  // Retorna 404 se o usuário não for encontrado
        }

        // Busca os orçamentos do usuário
        List<Orcamento> orcamentos = orcamentoService.getOrcamentosByUsuario(usuarioId);
        return ResponseEntity.ok(orcamentos);  // Retorna os orçamentos encontrados
    }
}
