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

    public Optional<User> getUserByEmail(String email) {
     return userRepository.findByEmail(email);

    }

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

        if (userDetails.getNome() != null) {
            updatedUser.setNome(userDetails.getNome());
        }
        if (userDetails.getEmail() != null) {
            updatedUser.setEmail(userDetails.getEmail());
        }
        if (userDetails.getSenha() != null) {
            updatedUser.setSenha(userDetails.getSenha());
        }
        if (userDetails.getCodStatus() != null) {
            updatedUser.setCodStatus(userDetails.getCodStatus());
        }
        if (userDetails.getCpf() != null) {
            updatedUser.setCpf(userDetails.getCpf());  // Campo CPF
        }
        if (userDetails.getTelefone() != null) {
            updatedUser.setTelefone(userDetails.getTelefone());  // Campo telefone
        }
        if (userDetails.getEndereco() != null) {
            updatedUser.setEndereco(userDetails.getEndereco());  // Campo endereço
        }
        if (userDetails.getCidade() != null) {
            updatedUser.setCidade(userDetails.getCidade());  // Campo cidade
        }
        if (userDetails.getBairro() != null) {
            updatedUser.setBairro(userDetails.getBairro());  // Campo bairro
        }
        if (userDetails.getEstado() != null) {
            updatedUser.setEstado(userDetails.getEstado());  // Campo estado
        }
        if (userDetails.getData_nascimento() != null) {
            updatedUser.setData_nascimento(userDetails.getData_nascimento());  // Data de nascimento
        }
        updatedUser.setAdmin(userDetails.isAdmin());

        return userRepository.save(updatedUser);


    }



    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


}
