package com.mobiauto.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OportunidadeRequestDTO {
    private Long clienteId;
    private Long veiculoId;
    private Long usuarioId;
    private Long revendaId;
    private String status; // "NOVO", "EM_ATENDIMENTO", "CONCLUIDO"
    private String motivoConclusao;

    public OportunidadeRequestDTO(long l, long l1, long l2, String emAtendimento, Object o) {
    }
}