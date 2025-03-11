package com.mobiauto.backend.dto;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        String cargo,
        Long revendaId,
        LocalDateTime dataUltimaAtribuicao
) {}