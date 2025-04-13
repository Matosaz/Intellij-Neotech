package com.backend.neotech.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;

@Table(name = "orcamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Time horaColeta;

    @Column(nullable = false)
    private Date dataColeta;

    @Column(nullable = false, name = "metodo_contato")
    private String metodoContato;

    @Column(nullable = false, name = "aceita_contato")
    private Boolean aceitaContato;

    @Column(nullable = false)
    private LocalDateTime coletaGeradaEm;


    @ManyToOne
    @JoinColumn(name ="id_usuario", referencedColumnName = "id")
    private User usuario;
    @PrePersist
    public void prePersist() {
        if (coletaGeradaEm == null) {
            coletaGeradaEm = LocalDateTime.now();  // Atribui a data e hora atuais
        }
    }
}
