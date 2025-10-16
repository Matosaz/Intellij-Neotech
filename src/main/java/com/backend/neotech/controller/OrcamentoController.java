package com.backend.neotech.controller;

import com.backend.neotech.exceptions.BadRequest;
import com.backend.neotech.model.Orcamento;
import com.backend.neotech.model.User;
import com.backend.neotech.repository.UserRepository;
import com.backend.neotech.service.EmailService;
import com.backend.neotech.service.OrcamentoService;
import com.backend.neotech.service.PixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v1/orcamentos")
public class OrcamentoController {
    private static final String CHAVE_PIX = "442.042.038-33";
    private static final String NOME_RECEBEDOR = "Matheus Pires";
    private static final String CIDADE = "Barueri";
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    private final OrcamentoService orcamentoService;
    @Autowired
    private PixService pixService;

    @Autowired
    public OrcamentoController(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }
    //Gera QR Code PIX para um orçamento específico  //
    // ✅ ENDPOINTS PIX BASEADOS NO PixService

    /**
     * GET - Gera QR Code PIX para orçamento com seu valor total
     */
    // =================


    /** POST - Gera QR Code PIX usando valor personalizado */
    @PostMapping("/{id}/pix")
    public ResponseEntity<?> gerarPixComValorPersonalizado(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> requestBody) {

        Double valor = extrairValorDoRequest(requestBody);
        return gerarPixInterno(id, valor);
    }

    /** GET - Debug completo do PIX para um orçamento */
    @GetMapping("/{id}/debug-pix")
    public ResponseEntity<?> debugPix(@PathVariable Long id) {
        Optional<Orcamento> orcamentoOpt = orcamentoService.buscarPorId(id);
        if (orcamentoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Orçamento não encontrado", "id", id));
        }

        Orcamento orcamento = orcamentoOpt.get();
        Double valor = orcamento.getValorTotal();
        if (valor == null || valor <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Orçamento sem valor definido", "orcamentoId", id));
        }

        String payload = pixService.gerarPayloadPix(valor);
        String qrCodeBase64 = pixService.gerarQRCodePix(valor);

        return ResponseEntity.ok(Map.of(
                "orcamento", Map.of(
                        "id", orcamento.getId(),
                        "valorTotal", valor,
                        "cliente", orcamento.getUsuario().getNome(),
                        "status", orcamento.getCodStatus()
                ),
                "pix", Map.of(
                        "payload", payload,
                        "qrCodeBase64", qrCodeBase64,
                        "chavePix", CHAVE_PIX,
                        "nomeRecebedor", NOME_RECEBEDOR,
                        "cidade", CIDADE
                ),
                "debug", Map.of(
                        "timestamp", System.currentTimeMillis(),
                        "payloadValido", pixService.validarPayload(payload)
                )
        ));
    }

    // Endpoint para gerar PIX
    @GetMapping("/{id}/pix")
    public ResponseEntity<?> gerarPix(@PathVariable Long id) {
        Optional<Orcamento> orcamentoOpt = orcamentoService.buscarPorId(id);

        if (orcamentoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Orcamento orcamento = orcamentoOpt.get();

        // Calcular valor total (exemplo: R$ 2,00 por kg)

        try {
            Map<String, Object> pixData = orcamentoService.gerarQRCodePix(orcamento.getValorTotal());
            return ResponseEntity.ok(pixData);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Erro ao gerar QR Code PIX",
                            "message", e.getMessage()
                    ));
        }
    }
    // Endpoint para debug do payload PIX
    @GetMapping("/debug-payload")
    public ResponseEntity<?> debugPayload(@RequestParam Double valor) {
        try {
            // Gera o payload e QR Code
            Map<String, Object> pixData = orcamentoService.gerarQRCodePix(valor);
            String payload = (String) pixData.get("payload");

            // Valida o payload
            boolean valido = orcamentoService.validarPayloadPix(payload);

            // Retorna informações completas
            return ResponseEntity.ok(Map.of(
                    "payload", payload,
                    "valor", valor,
                    "valido", valido,
                    "qrCodeBase64", pixData.get("qrCodeBase64"),
                    "txid", pixData.get("txid"),
                    "chavePix", pixData.get("chavePix"),
                    "nomeRecebedor", pixData.get("nomeRecebedor")
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    // Endpoint apenas para testar o payload
    @GetMapping("/teste-pix")
    public ResponseEntity<?> testarPix(@RequestParam Double valor) {
        try {
            Map<String, Object> pixData = orcamentoService.gerarQRCodePix(valor);
            return ResponseEntity.ok(pixData);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }


    // ====================== MÉTODOS AUXILIARES ======================

    private ResponseEntity<?> gerarPixInterno(Long id, Double valorPersonalizado) {
        try {
            Optional<Orcamento> orcamentoOpt = orcamentoService.buscarPorId(id);
            if (orcamentoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Orçamento não encontrado", "id", id));
            }

            Orcamento orcamento = orcamentoOpt.get();
            Double valor = valorPersonalizado != null ? valorPersonalizado : orcamento.getValorTotal();

            if (valor == null || valor <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Valor inválido ou não definido",
                        "orcamentoId", id
                ));
            }

            String payload = pixService.gerarPayloadPix(valor);
            String qrCodeBase64 = pixService.gerarQRCodePix(valor);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orcamento", Map.of(
                            "id", orcamento.getId(),
                            "valorOriginal", orcamento.getValorTotal(),
                            "cliente", orcamento.getUsuario().getNome(),
                            "status", orcamento.getCodStatus()
                    ),
                    "pix", Map.of(
                            "qrCodeBase64", qrCodeBase64,
                            "payload", payload,
                            "valorCobrado", valor,
                            "chavePix", CHAVE_PIX,
                            "nomeRecebedor", NOME_RECEBEDOR,
                            "cidade", CIDADE,
                            "tipo", valor.equals(orcamento.getValorTotal()) ? "VALOR_ORIGINAL" : "VALOR_PERSONALIZADO"
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "orcamentoId", id
            ));
        }
    }

    private Double extrairValorDoRequest(Map<String, Object> requestBody) {
        if (requestBody == null || !requestBody.containsKey("valor")) return null;
        Object valorObj = requestBody.get("valor");
        if (valorObj instanceof Number) return ((Number) valorObj).doubleValue();
        try { return Double.parseDouble(valorObj.toString()); }
        catch (Exception e) { return null; }
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

            User user = usuarioOptional.get();
            emailService.sendConfirmationEmail(
                    user.getEmail(),
                    user.getNome(),
                    savedOrcamento.getDataColeta(),
                    savedOrcamento.getHoraColeta(),
                    savedOrcamento.getEndereco(),
                    savedOrcamento.getNumero(),
                    savedOrcamento.getBairro(),
                    savedOrcamento.getCidade(),
                    savedOrcamento.getEstado()
            );


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
            orcamentoExistente.setCodStatus(orcamentoAtualizado.getCodStatus());


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