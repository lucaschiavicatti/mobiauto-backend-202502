package com.mobiauto.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevendaRequestDTO {
    private String cnpj;
    private String nomeSocial;
}