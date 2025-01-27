package com.backend.neotech.controller;

import com.backend.neotech.exceptions.BadRequest;
import com.backend.neotech.model.User;
import com.backend.neotech.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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

    // Listar todos os usuários
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok().body(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable(value = "id") String id) {
        try {
            User user = userService.getUserById(Long.parseLong(id));
            // Converte o avatar para Base64, caso exista
            String avatarBase64 = user.getAvatar() != null ? Base64.getEncoder().encodeToString(user.getAvatar()) : null;

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("name", user.getNome());
            response.put("email", user.getEmail());
            response.put("avatar", avatarBase64);

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
