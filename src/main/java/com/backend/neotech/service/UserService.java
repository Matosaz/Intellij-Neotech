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
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User updatedUser = user.get();
            updatedUser.setNome(userDetails.getNome());
            updatedUser.setEmail(userDetails.getEmail());
            updatedUser.setSenha(userDetails.getSenha());
            updatedUser.setAtivo(userDetails.isAtivo());
            updatedUser.setAdmin(userDetails.isAdmin());
            return userRepository.save(updatedUser);
        }
        return null; // Ou lançar uma exceção
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
