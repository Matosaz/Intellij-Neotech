package com.backend.neotech.controller;

import com.backend.neotech.exceptions.BadRequest;
import com.backend.neotech.model.User;
import com.backend.neotech.repository.ResetCodeRepository;
import com.backend.neotech.repository.UserRepository;
import com.backend.neotech.service.EmailService;
import com.backend.neotech.service.ResetCodeService;
import com.backend.neotech.service.UserService;
import org.springframework.http.HttpStatus;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final ResetCodeService resetCodeService;
    private final ResetCodeRepository resetCodeRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserController(ResetCodeRepository resetCodeRepository,ResetCodeService resetCodeService, UserService userService, UserRepository userRepository, EmailService emailService) {
        this.resetCodeRepository = resetCodeRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.resetCodeService = resetCodeService;

    }

    // Endpoint de login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        Optional<User> userOpt = userService.getUserByEmail(loginUser.getEmail());

        if (userOpt.isPresent() && userOpt.get().getSenha().equals(loginUser.getSenha())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login bem-sucedido!");
            response.put("id", String.valueOf(userOpt.get().getId()));
            response.put("nome", userOpt.get().getNome());
            response.put("email", userOpt.get().getEmail());
            response.put("isAdmin", String.valueOf(userOpt.get().isAdmin()));

            return ResponseEntity.ok().body(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Credenciais inválidas!"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "E-mail não encontrado."));
        }

        String code = String.format("%06d", new Random().nextInt(999999));

        // Armazena o código e o horário de criação
        resetCodeService.storeResetCode(email, code);

        // Envia o e-mail usando o EmailService
        emailService.sendResetCode(email, code);

        return ResponseEntity.ok(Map.of("success", true, "message", "Código enviado ao e-mail."));
    }
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyResetCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String newPassword = request.get("newPassword");

        boolean valid = resetCodeService.validateResetCode(email, code);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Código inválido ou expirado."));
        }

        userService.updatePassword(email, newPassword);
        return ResponseEntity.ok(Map.of("success", true, "message", "Senha redefinida com sucesso."));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT) // Código de erro 409 para conflito
                    .body(Collections.singletonMap("exists", exists)); // Pode retornar um JSON indicando a duplicidade
        }
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }

    // Listar todos os usuários
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok().body(userService.getAllUsers());
    }
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable(value = "id") String id) {
        try {
            User user = userService.getUserById(Long.parseLong(id));
            // Converte o avatar para Base64, caso exista
            String avatarBase64 = user.getAvatar() != null ? Base64.getEncoder().encodeToString(user.getAvatar()) : null;

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("nome", user.getNome());
            response.put("email", user.getEmail());
            response.put("avatar", avatarBase64);
            response.put("cidade", user.getCidade());
            response.put("estado", user.getEstado());
            response.put("bairro", user.getBairro());
            response.put("telefone", user.getTelefone());
            response.put("endereco", user.getEndereco());
            response.put("cep", user.getCep());
            response.put("cpf", user.getCpf());
            response.put("data_nascimento", user.getData_nascimento()); //Testemos para verificar se funcionará






            return ResponseEntity.ok().body(response);
        } catch (NumberFormatException ex) {
            throw new BadRequest("'" + id + "' não é um número inteiro válido. Por favor, forneça um valor inteiro, como 10.");
        }
    }

    // Criar um novo usuário
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/users").toUriString());
        return ResponseEntity.created(uri).body(userService.createUser(user));
    }

    // Atualizar um usuário existente
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable(value = "id") String id,
            @RequestPart("data") String userDetailsJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {
        try {
            User user = userService.getUserById(Long.valueOf(id));
            ObjectMapper objectMapper = new ObjectMapper();
            User userDetails = objectMapper.readValue(userDetailsJson, User.class);

            // Se um avatar for enviado como arquivo, processa-o
            if (avatarFile != null && !avatarFile.isEmpty()) {
                if (!avatarFile.getContentType().startsWith("image/")) {
                    throw new BadRequest("O arquivo enviado não é uma imagem válida.");
                }
                userDetails.setAvatar(avatarFile.getBytes()); // Armazena o avatar como byte[]
            }

            User updatedUser = userService.updateUser(Long.parseLong(id), userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (NumberFormatException ex) {
            throw new BadRequest("'" + id + "' não é um número inteiro válido.");
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao atualizar o usuário: " + ex.getMessage());
        }
    }

    // Deletar um usuário
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable(value = "id") String id) {
        try {
            userService.deleteUser(Long.parseLong(id));
            return ResponseEntity.noContent().build();
        } catch (NumberFormatException ex) {
            throw new BadRequest("'" + id + "' não é um número inteiro válido. Por favor, forneça um valor inteiro, como 10.");
        }
    }
}
