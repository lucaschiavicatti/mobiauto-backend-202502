package com.mobiauto.backend.dto;

import com.mobiauto.backend.model.Cargo;

public record UsuarioResponseDTO(Long id, String nome, String email, Cargo cargo, Long revendaId) {
}
