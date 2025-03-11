package com.mobiauto.backend.dto;

import lombok.Data;

@Data
public class OportunidadeRequestDTO {
    private Long clienteId;
    private Long veiculoId;
    private Long usuarioId;
    private Long revendaId;
    private String status; // "NOVO", "EM_ATENDIMENTO", "CONCLUIDO"
    private String motivoConclusao;
}