package com.mobiauto.backend.dto;

import java.time.LocalDateTime;

public record OportunidadeResponseDTO(
        Long id,
        Long clienteId,
        Long veiculoId,
        Long usuarioId,
        Long revendaId,
        String status,
        String motivoConclusao,
        LocalDateTime dataAtribuicao,
        LocalDateTime dataConclusao
) {}