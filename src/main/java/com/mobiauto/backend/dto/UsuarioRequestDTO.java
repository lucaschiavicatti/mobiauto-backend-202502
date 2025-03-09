package com.mobiauto.backend.dto;

import com.mobiauto.backend.model.Usuario;
import lombok.Data;

@Data
public class UsuarioRequestDTO {
    private String nome;
    private String email;
    private String senha;
    private Usuario.Cargo cargo;
    private Long revendaId;
}