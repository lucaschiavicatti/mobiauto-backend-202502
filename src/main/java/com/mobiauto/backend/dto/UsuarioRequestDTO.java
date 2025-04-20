package com.mobiauto.backend.dto;

import com.mobiauto.backend.model.Cargo;
import lombok.Data;

@Data
public class UsuarioRequestDTO {
    private String nome;
    private String email;
    private String senha;
    private Cargo cargo;
    private Long revendaId;

    public UsuarioRequestDTO(String nome, String email, String senha, Cargo cargo, Long revendaId) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.cargo = cargo;
        this.revendaId = revendaId;
    }
}