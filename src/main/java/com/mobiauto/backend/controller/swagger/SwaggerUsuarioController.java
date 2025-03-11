package com.mobiauto.backend.controller.swagger;

import com.mobiauto.backend.dto.UsuarioRequestDTO;
import com.mobiauto.backend.dto.UsuarioResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
public interface SwaggerUsuarioController extends SwaggerCrudController<UsuarioResponseDTO, Long, UsuarioRequestDTO> {

    @Operation(summary = "Busca um usuário por e-mail", description = "Retorna os detalhes de um usuário específico pelo e-mail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    ResponseEntity<UsuarioResponseDTO> buscarPorEmail(String email);
}