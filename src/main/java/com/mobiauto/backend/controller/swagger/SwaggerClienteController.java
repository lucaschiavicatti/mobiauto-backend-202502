package com.mobiauto.backend.controller.swagger;

import com.mobiauto.backend.dto.ClienteRequestDTO;
import com.mobiauto.backend.dto.ClienteResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Clientes", description = "Endpoints para gerenciamento de clientes")
public interface SwaggerClienteController extends SwaggerCrudController<ClienteResponseDTO, Long, ClienteRequestDTO> {
}