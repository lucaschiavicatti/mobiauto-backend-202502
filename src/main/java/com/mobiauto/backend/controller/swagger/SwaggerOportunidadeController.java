package com.mobiauto.backend.controller.swagger;

import com.mobiauto.backend.dto.OportunidadeRequestDTO;
import com.mobiauto.backend.dto.OportunidadeResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Oportunidades", description = "Endpoints para gerenciamento de oportunidades")
public interface SwaggerOportunidadeController extends SwaggerCrudController<OportunidadeResponseDTO, Long, OportunidadeRequestDTO> {
}