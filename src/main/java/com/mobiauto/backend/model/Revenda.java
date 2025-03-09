package com.mobiauto.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Revenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String cnpj;

    @Column(nullable = false)
    private String nomeSocial;
}