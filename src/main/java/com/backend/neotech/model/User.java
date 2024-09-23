package com.backend.neotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false)
    private String senha;

    @Column(name = "cod_status", nullable = false)
    private String codStatus = "A"; // Valor padrão

    @Column(name = "is_Admin", nullable = false) // Ajuste no nome da coluna
    private boolean admin = false; // Valor padrão

}
