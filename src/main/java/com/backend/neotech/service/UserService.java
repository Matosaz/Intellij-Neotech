package com.backend.neotech.service;

import com.backend.neotech.exceptions.NotFound;
import com.backend.neotech.model.User;
import com.backend.neotech.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFound("Usuário com ID " + id + " não encontrado."));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
        // Adicionando log para depuração
        System.out.println("Criando usuário: " + user);
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User updatedUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFound("Usuário com ID " + id + " não encontrado."));

        updatedUser.setNome(userDetails.getNome());
        updatedUser.setEmail(userDetails.getEmail());
        updatedUser.setSenha(userDetails.getSenha());
        updatedUser.setCodStatus(userDetails.getCodStatus());
        updatedUser.setAdmin(userDetails.isAdmin());
        return userRepository.save(updatedUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
