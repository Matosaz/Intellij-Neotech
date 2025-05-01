package com.backend.neotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario") // Nome da tabela em minúsculas
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true, length = 11)
    private String cpf;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = true)
    private Date data_nascimento;

    @Column(nullable = true)
    private String telefone;

    @Column(nullable = true)
    private String cep;

    @Column(nullable = true)
    private String endereco;

    @Column(nullable = true)
    private String bairro;

    @Column(nullable = true)
    private String cidade;

    @Column(nullable = true)
    private String estado;

    @Column(name = "ultima_modificacao")
    private LocalDateTime ultimaModificacao;

    //gênero
    @Column(nullable = true)
    private String genero;

    @Column(name = "avatar", nullable = true)
    @Lob // Define que o campo será armazenado como BLOB
    private byte[] avatar;

    @Column(name = "cod_status", nullable = false)
    private String codStatus = "ATIVO"; // Valor padrão

    @Column(name = "is_Admin", nullable = false) // Ajuste no nome da coluna
    private boolean admin = false; // Valor padrão

}
