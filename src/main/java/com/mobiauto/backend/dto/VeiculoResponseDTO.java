package com.mobiauto.backend.dto;

public record VeiculoResponseDTO(
        Long id,
        String marca,
        String modelo,
        String versao,
        Integer anoModelo,
        Long revendaId
) {}
