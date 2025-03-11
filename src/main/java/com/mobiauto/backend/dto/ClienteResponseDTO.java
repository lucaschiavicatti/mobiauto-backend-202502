package com.mobiauto.backend.dto;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        Long revendaId
) {}
