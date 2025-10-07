package com.backend.neotech.service;
import com.backend.neotech.exceptions.NotFound;
import com.backend.neotech.model.User;
import com.backend.neotech.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.password.PasswordEncoder;


@Service
public class UserService {



    @Autowired
    private EmailService emailService;

    // Armazena os códigos temporários de recuperação
    private final Map<String, ResetCodeData> resetCodes = new ConcurrentHashMap<>();

    private static class ResetCodeData {
        String code;
        LocalDateTime expiresAt;

        ResetCodeData(String code, LocalDateTime expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }

    // Armazena o código de recuperação
    public void storeResetCode(String email, String code) {
        resetCodes.put(email, new ResetCodeData(code, LocalDateTime.now().plusMinutes(10)));
    }

    // Valida o código enviado pelo usuário
    public boolean validateResetCode(String email, String code) {
        ResetCodeData data = resetCodes.get(email);
        return data != null && data.code.equals(code) && LocalDateTime.now().isBefore(data.expiresAt);
    }

    // Atualiza a senha do usuário

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ResetCodeService resetCodeService;

    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFound("Usuário com email " + email + " não encontrado."));
        user.setSenha(passwordEncoder.encode(newPassword)); // ✅ Corrigido
        userRepository.save(user);

        resetCodeService.removeResetCode(email);
    }


    @Autowired
    private UserRepository userRepository;
    public  Optional<User> getUserByEmail(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            throw new NotFound("Usuário não encontrado para o email:" + email);
        }
        return optionalUser;
    }




    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFound("Usuário com ID " + id + " não encontrado."));
    }
//tESTE 2
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public Integer getUserPoints(Long userId) {
        return userRepository.findTotalPointsByUserId(userId);
    }

    public User createUser(User user) {
        // Adicionando log para depuração
        System.out.println("Criando usuário: " + user);
        return userRepository.save(user);
    }

    //gênero inserido
    // No UserService.java

    public User updateUser(Long id, User userDetails) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFound("Usuário com ID " + id + " não encontrado."));
        System.out.println("Status recebido: " + userDetails.getCodStatus());
        // --- Início dos logs para depuração (Mantenha se ainda tiver dúvidas) ---
        System.out.println("--- DEBUG INÍCIO UPDATE USER ---");
        System.out.println("Existing User Admin ANTES da atualização: " + existingUser.getAdmin());
        System.out.println("User Details Admin Recebido na Requisição: " + userDetails.getAdmin());
        // --- Fim dos logs ---

// 👉 ADICIONE AQUI
        if (userDetails.getCodStatus() != null) {
            existingUser.setCodStatus(userDetails.getCodStatus().toUpperCase());
            System.out.println("Status alterado para: " + existingUser.getCodStatus());
        } else {
            System.out.println("CodStatus não enviado — mantendo: " + existingUser.getCodStatus());
        }
        // Lista de campos que podem ser atualizados
        List<String> updatableFields = Arrays.asList(
                "nome", "email", "cpf", "telefone", "cep",
                "endereco", "cidade", "bairro", "estado",
                "data_nascimento", "genero", "avatar" // Remova "isAdmin" daqui, vamos tratá-lo separadamente
        );

        // Itera e atualiza apenas os campos permitidos, se não forem nulos no userDetails
        updatableFields.forEach(field -> {
            try {
                Method getter = User.class.getMethod("get" + capitalize(field));
                Method setter = User.class.getMethod("set" + capitalize(field), getter.getReturnType());

                Object newValue = getter.invoke(userDetails);
                if (newValue != null) { // Só atualiza se o novo valor não for nulo
                    setter.invoke(existingUser, newValue);
                }
            } catch (Exception e) {
                // É uma boa prática logar exceções aqui, mas sem lançá-las para não interromper a atualização
                System.err.println("Erro ao refletir campo " + field + ": " + e.getMessage());
            }
        });

        // TRATAMENTO ESPECÍFICO PARA O CAMPO 'ADMIN'
        // Este é o ponto chave: só alteramos se o valor veio na requisição (não é null)
        if (userDetails.getAdmin() != null) {
            existingUser.setAdmin(userDetails.getAdmin());
            System.out.println("Existing User Admin APÓS SET (admin explicitamente enviado): " + existingUser.getAdmin());
        } else {
            // Se userDetails.getAdmin() for null, significa que o campo NÃO FOI ENVIADO
            // na requisição, e, portanto, o valor existente de existingUser.admin
            // não é alterado.
            System.out.println("User Details Admin é null, mantendo o valor existente: " + existingUser.getAdmin());
        }

        // Atualiza a data de última modificação
        existingUser.setUltimaModificacao(LocalDateTime.now());

        User updatedUser = userRepository.save(existingUser);

        // --- Continuação dos logs para depuração ---
        System.out.println("Existing User Admin APÓS SAVE: " + updatedUser.getAdmin());
        System.out.println("--- DEBUG FIM UPDATE USER ---");
        // --- Fim dos logs ---

        return updatedUser;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


}
