package com.backend.neotech.controller;
import com.backend.neotech.exceptions.BadRequest;
import com.backend.neotech.exceptions.NotFound;
import com.backend.neotech.model.User;
import com.backend.neotech.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static javax.swing.UIManager.put;

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
            // Aqui você pode gerar um token ou qualquer outro mecanismo de autenticação


            Map<String, String> response = new HashMap<>();
            response.put("message", "Login bem-sucedido!");  // Mensagem de sucesso
            response.put("nome", userOpt.get().getNome());   // Nome do usuário

            return ResponseEntity.ok().body(response);




        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HashMap<String, String>() {{
                put("message","Credenciais inválidas!");
            }});
        }
    }


    // Listar todos os usuários
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok().body(userService.getAllUsers());
    }

    // Obter um usuário por ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable(value = "id") String id) {
        try {
            return ResponseEntity.ok().body(userService.getUserById(Long.parseLong(id)));
        } catch (NumberFormatException ex) {
            throw new BadRequest("'" + id + "' não é um número inteiro válido. Por favor, forneça um valor inteiro, como 10");
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
    public ResponseEntity<User> updateUser(@PathVariable(value = "id") String id, @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(Long.parseLong(id), userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (NumberFormatException ex) {
            throw new BadRequest("'" + id + "' não é um número inteiro válido. Por favor, forneça um valor inteiro, como 10");
        }
    }

    // Deletar um usuário
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable(value = "id") String id) {
        try {
            userService.deleteUser(Long.parseLong(id));
            return ResponseEntity.noContent().build();
        } catch (NumberFormatException ex) {
            throw new BadRequest("'" + id + "' não é um número inteiro válido. Por favor, forneça um valor inteiro, como 10");
        }
    }
}
