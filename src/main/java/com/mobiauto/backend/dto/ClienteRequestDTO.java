package com.mobiauto.backend.dto;

import lombok.Data;

@Data
public class ClienteRequestDTO {
    private String nome;
    private String email;
    private String telefone;
    private Long revendaId;
}