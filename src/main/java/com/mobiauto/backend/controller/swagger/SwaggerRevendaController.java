package com.mobiauto.backend.controller.swagger;

import com.mobiauto.backend.dto.RevendaRequestDTO;
import com.mobiauto.backend.dto.RevendaResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Revendas", description = "Endpoints para gerenciamento de revendas")
public interface SwaggerRevendaController extends SwaggerCrudController<RevendaResponseDTO, Long, RevendaRequestDTO> {
}