package com.backend.neotech.controller;

import com.backend.neotech.exceptions.BadRequest;
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
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/orcamentos")
public class OrcamentoController {

    @Autowired
    private UserRepository userRepository;

    private final OrcamentoService orcamentoService;

    @Autowired
    public OrcamentoController(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }

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
        Optional<User> usuarioOptional = userRepository.findById(orcamento.getUsuario().getId());
        if (!usuarioOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Usuário não encontrado.");
        }

        try {
            Orcamento savedOrcamento = orcamentoService.salvarOrcamento(orcamento);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(savedOrcamento.getId())
                    .toUri();
            return ResponseEntity.created(location).body("Orçamento criado com sucesso.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao salvar o orçamento.");
        }
    }

    @GetMapping
    public ResponseEntity<List<Orcamento>> getAllOrcamentos() {
        List<Orcamento> orcamentos = orcamentoService.listarOrcamentos();
        return ResponseEntity.ok(orcamentos);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Orcamento>> getOrcamentosByUsuario(@PathVariable Long usuarioId) {
        Optional<User> usuarioOptional = userRepository.findById(usuarioId);
        if (!usuarioOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.emptyList());
        }
        List<Orcamento> orcamentos = orcamentoService.getOrcamentosByUsuario(usuarioId);
        return ResponseEntity.ok(orcamentos);
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Orcamento>> getOrcamentosByCategoria(@PathVariable Long categoriaId) {
        List<Orcamento> orcamentos = orcamentoService.getOrcamentosByCategoria(categoriaId);
        if (orcamentos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        return ResponseEntity.ok(orcamentos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> atualizarOrcamento(
            @PathVariable Long id,
            @RequestBody Orcamento orcamentoAtualizado) {

        Optional<Orcamento> orcamentoExistenteOpt = orcamentoService.buscarPorId(id);

        if (!orcamentoExistenteOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Orçamento não encontrado."));
        }

        // Validações básicas
        if (orcamentoAtualizado.getMetodoContato() == null || orcamentoAtualizado.getMetodoContato().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Método de contato é obrigatório."));
        }
        if (orcamentoAtualizado.getHoraColeta() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Hora da coleta é obrigatória."));
        }
        if (orcamentoAtualizado.getDataColeta() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Data da coleta é obrigatória."));
        }
        if (orcamentoAtualizado.getAceitaContato() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Aceite de contato é obrigatório."));
        }
        try {
            Orcamento orcamentoExistente = orcamentoExistenteOpt.get();

            orcamentoExistente.setMetodoContato(orcamentoAtualizado.getMetodoContato());
            orcamentoExistente.setHoraColeta(orcamentoAtualizado.getHoraColeta());
            orcamentoExistente.setDataColeta(orcamentoAtualizado.getDataColeta());
            orcamentoExistente.setAceitaContato(orcamentoAtualizado.getAceitaContato());
            orcamentoExistente.setCategorias(orcamentoAtualizado.getCategorias());
            orcamentoExistente.setCep(orcamentoAtualizado.getCep());
            orcamentoExistente.setEndereco(orcamentoAtualizado.getEndereco());
            orcamentoExistente.setNumero(orcamentoAtualizado.getNumero());
            orcamentoExistente.setBairro(orcamentoAtualizado.getBairro());
            orcamentoExistente.setCidade(orcamentoAtualizado.getCidade());
            orcamentoExistente.setEstado(orcamentoAtualizado.getEstado());
            orcamentoExistente.setTelefone(orcamentoAtualizado.getTelefone());


            orcamentoService.salvarOrcamento(orcamentoExistente);

            return ResponseEntity.ok(Map.of("message", "Orçamento atualizado com sucesso!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erro ao atualizar o orçamento."));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrcamento(@PathVariable(value = "id") String id) {
        try {
            orcamentoService.deleteOrcamento(Long.parseLong(id));
            return ResponseEntity.noContent().build();
        } catch (NumberFormatException ex) {
            throw new BadRequest("'" + id + "' não é um número inteiro válido. Por favor, forneça um valor inteiro, como 10.");
        }
    }
}