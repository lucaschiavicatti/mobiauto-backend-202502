package com.mobiauto.backend.controller.swagger;

import com.mobiauto.backend.dto.VeiculoRequestDTO;
import com.mobiauto.backend.dto.VeiculoResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Veículos", description = "Endpoints para gerenciamento de veículos")
public interface SwaggerVeiculoController extends SwaggerCrudController<VeiculoResponseDTO, Long, VeiculoRequestDTO> {
}