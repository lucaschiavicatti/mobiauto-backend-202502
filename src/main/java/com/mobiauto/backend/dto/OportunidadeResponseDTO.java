package com.mobiauto.backend.dto;

import java.time.LocalDateTime;

public record OportunidadeResponseDTO(
        Long id,
        ClienteDTO cliente,
        VeiculoDTO veiculo,
        Long usuarioId,
        Long revendaId,
        String status,
        String motivoConclusao,
        LocalDateTime dataAtribuicao,
        LocalDateTime dataConclusao
) {
    public record ClienteDTO(Long id, String nome, String email, String telefone) {}
    public record VeiculoDTO(Long id, String marca, String modelo, String versao, Integer anoModelo) {}
}