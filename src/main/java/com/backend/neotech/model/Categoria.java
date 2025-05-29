package com.backend.neotech.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Table(name = "categoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "nome")
    private String nome;

    @Column(nullable = true, name = "descricao")
    private String descricao;

    @Column(name = "cod_status", nullable = false)
    private String codStatus = "ATIVO"; // Valor padrão

    @Column(name = "preco_por_kg", precision = 10, scale = 2)
    private BigDecimal precoPorKg;


    }
