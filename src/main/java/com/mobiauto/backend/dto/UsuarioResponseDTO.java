package com.mobiauto.backend.dto;

import com.mobiauto.backend.model.Cargo;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        Cargo cargo,
        Long revendaId,
        LocalDateTime dataUltimaAtribuicao
) {}