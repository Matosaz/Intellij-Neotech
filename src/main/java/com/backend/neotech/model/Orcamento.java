package com.backend.neotech.model;
import com.backend.neotech.model.Categoria;
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

    @ManyToOne(fetch = FetchType.EAGER)  // eager para já trazer categoria quando buscar orçamento
    @JoinColumn(name = "id_categoria", nullable = true)  // ou false, se obrigatório
    private Categoria categoria;

    @Column(nullable = false, name = "hora_coleta")
    private Time horaColeta;

    @Column(nullable = false, name = "data_coleta")
    private Date dataColeta;

    @Column(nullable = false, name = "metodo_contato")
    private String metodoContato;

    @Column(nullable = false, name = "aceita_contato")
    private Boolean aceitaContato;

    @Column(nullable = false, name = "coleta_gerada_em")
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
