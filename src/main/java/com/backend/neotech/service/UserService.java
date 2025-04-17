package com.backend.neotech.service;
import com.backend.neotech.exceptions.NotFound;
import com.backend.neotech.model.User;
import com.backend.neotech.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;


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
    private ResetCodeService resetCodeService;

    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFound("Usuário com email " + email + " não encontrado."));
        user.setSenha(newPassword);
        userRepository.save(user);

        resetCodeService.removeResetCode(email); // remove do banco após uso
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

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
        // Adicionando log para depuração
        System.out.println("Criando usuário: " + user);
        return userRepository.save(user);
    }

    //gênero inserido

    public User updateUser(Long id, User userDetails) {
        User updatedUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFound("Usuário com ID " + id + " não encontrado."));

        if (userDetails.getNome() != null) {
            updatedUser.setNome(userDetails.getNome());
        }
        if (userDetails.getGenero() != null) {
            updatedUser.setGenero(userDetails.getGenero());
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
        if (userDetails.getCep() != null) {
            updatedUser.setCep(userDetails.getCep());  // Campo CEP
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
        if (userDetails.getAvatar() != null) {
            updatedUser.setAvatar(userDetails.getAvatar());
        }

        updatedUser.setUltimaModificacao(LocalDateTime.now());

        return userRepository.save(updatedUser);


    }



    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


}
