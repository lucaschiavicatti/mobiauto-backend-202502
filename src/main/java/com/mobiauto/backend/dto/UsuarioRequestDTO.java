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
}