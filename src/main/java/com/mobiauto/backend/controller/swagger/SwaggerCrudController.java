package com.mobiauto.backend.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
public interface SwaggerCrudController<T, ID, R> {

    @Operation(summary = "Lista todos os recursos", description = "Retorna a lista de recursos visíveis ao usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    ResponseEntity<List<T>> listar();

    @Operation(summary = "Busca um recurso por ID", description = "Retorna os detalhes de um recurso específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recurso encontrado"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    ResponseEntity<T> buscarPorId(ID id);

    @Operation(summary = "Cria um novo recurso", description = "Cria um novo recurso no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Recurso criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    ResponseEntity<T> criar(R requestDto);

    @Operation(summary = "Atualiza um recurso", description = "Atualiza os dados de um recurso existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recurso atualizado"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    ResponseEntity<T> atualizar(ID id, R requestDto);

    @Operation(summary = "Exclui um recurso", description = "Remove um recurso do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Recurso excluído"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    ResponseEntity<Void> deletar(ID id);
}
