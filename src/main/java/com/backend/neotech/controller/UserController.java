package com.backend.neotech.controller;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.backend.neotech.exceptions.BadRequest;
import com.backend.neotech.model.User;
import com.backend.neotech.repository.ResetCodeRepository;
import com.backend.neotech.repository.UserRepository;
import com.backend.neotech.repository.UserSummary;

import com.backend.neotech.service.EmailService;
import com.backend.neotech.service.ResetCodeService;
import com.backend.neotech.service.UserService;
import com.fasterxml.jackson.databind.MapperFeature;
import org.hibernate.query.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final PasswordEncoder passwordEncoder;
//Corrigido
    private final ResetCodeService resetCodeService;
    private final ResetCodeRepository resetCodeRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserController(PasswordEncoder passwordEncoder, ResetCodeRepository resetCodeRepository, ResetCodeService resetCodeService, UserService userService, UserRepository userRepository, EmailService emailService) {
        this.passwordEncoder = passwordEncoder;
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

        if (userOpt.isPresent() && passwordEncoder.matches(loginUser.getSenha(), userOpt.get().getSenha())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login bem-sucedido!");
            response.put("id", String.valueOf(userOpt.get().getId()));
            response.put("nome", userOpt.get().getNome());
            response.put("email", userOpt.get().getEmail());
            response.put("isAdmin", String.valueOf(userOpt.get().getAdmin()));

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
//---------------CONSULTAS OTIMIZADAS PARA A TELA DE RANKING-----------------------//
    //Teste

    @GetMapping("/ranking")
    public ResponseEntity<List<UserSummary>> getRanking() {
        List<UserSummary> ranking = userRepository.findAllUsersWithTotalPoints();
        return ResponseEntity.ok(ranking);
    }
    @GetMapping("/me/avatar")
    public ResponseEntity<String> getLoggedUserAvatar(@RequestParam Long userId) {
        Optional<byte[]> avatarOpt = userRepository.findAvatarByUserId(userId);

        if (avatarOpt.isEmpty() || avatarOpt.get() == null) {
            return ResponseEntity.notFound().build();
        }

        String avatarBase64 = Base64.getEncoder().encodeToString(avatarOpt.get());
        return ResponseEntity.ok(avatarBase64);
    }
//--------------------------------------------------------------------------------//

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
            response.put("ultimaModificacao", user.getUltimaModificacao());
            response.put("cpf", user.getCpf());
            response.put("data_nascimento", user.getData_nascimento()); //Testemos para verificar se funcionará
            response.put("data_criacao", user.getDataCriacao()); //Testemos para verificar se funcionará







            return ResponseEntity.ok().body(response);
        } catch (NumberFormatException ex) {
            throw new BadRequest("'" + id + "' não é um número inteiro válido. Por favor, forneça um valor inteiro, como 10.");
        }
    }



    // Criar um novo usuário
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        user.setSenha(passwordEncoder.encode(user.getSenha())); // Criptografar senha
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/users").toUriString());
        return ResponseEntity.created(uri).body(userService.createUser(user));
    }
    @Autowired
    private ObjectMapper objectMapper;

    // Atualizar um usuário existente
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<User> updateUserMultipart(
            @PathVariable(value = "id") String id,
            @RequestPart("data") String userDetailsJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {

        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        try {
            User user = userService.getUserById(Long.valueOf(id));
            User userDetails = objectMapper.readValue(userDetailsJson, User.class);

            if (avatarFile != null && !avatarFile.isEmpty()) {
                if (!avatarFile.getContentType().startsWith("image/")) {
                    throw new BadRequest("O arquivo enviado não é uma imagem válida.");
                }
                userDetails.setAvatar(avatarFile.getBytes());
            }

            User updatedUser = userService.updateUser(Long.parseLong(id), userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (NumberFormatException ex) {
            throw new BadRequest("'" + id + "' não é um número inteiro válido.");
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao atualizar o usuário (web): " + ex.getMessage());
        }
    }

    @PutMapping(value = "/test-update/{id}", consumes = "application/json")
    public ResponseEntity<?> testUpdateUserJson(
            @PathVariable(value = "id") String id,
            @RequestBody Map<String, Object> payload) { // Use Map para ver o JSON cru

        System.out.println("--- TESTE DE PAYLOAD ---");
        System.out.println("Payload recebido (como Map): " + payload);
        System.out.println("Valor de 'admin' no payload (se existir): " + payload.get("admin"));
        System.out.println("--- FIM TESTE DE PAYLOAD ---");

        return ResponseEntity.ok(payload); // Apenas retorne o payload para inspeção
    }
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<User> updateUserJson(
            @PathVariable(value = "id") String id,
            @RequestBody User userDetails) {

        try {
            User user = userService.getUserById(Long.valueOf(id));
            System.out.println("Dados recebidos: " + userDetails); // Log importante

            // Preserva o avatar existente se não for enviado
            if (userDetails.getAvatar() == null) {
                userDetails.setAvatar(user.getAvatar());
            }

            User updatedUser = userService.updateUser(Long.parseLong(id), userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (NumberFormatException ex) {
            throw new BadRequest("'" + id + "' não é um número inteiro válido.");
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao atualizar o usuário (Flutter): " + ex.getMessage());
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
