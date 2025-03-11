package com.mobiauto.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VeiculoRequestDTO {
    private String marca;
    private String modelo;
    private String versao;
    private Integer anoModelo;
    private Long revendaId;
}