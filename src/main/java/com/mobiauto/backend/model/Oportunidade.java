package com.mobiauto.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Oportunidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "revenda_id", nullable = false)
    private Revenda revenda;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusOportunidade status;

    @Column
    private String motivoConclusao;

    @Column(nullable = false)
    private LocalDateTime dataAtribuicao;

    @Column
    private LocalDateTime dataConclusao;
}

