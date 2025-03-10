package com.mobiauto.backend.dto;

import lombok.Data;

@Data
public class VeiculoRequestDTO {
    private String marca;
    private String modelo;
    private String versao;
    private Integer anoModelo;
    private Long revendaId;
}