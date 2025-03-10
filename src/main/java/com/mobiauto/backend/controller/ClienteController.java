package com.mobiauto.backend.controller;

import com.mobiauto.backend.dto.ClienteRequestDTO;
import com.mobiauto.backend.dto.ClienteResponseDTO;
import com.mobiauto.backend.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listar() {
        return ResponseEntity.ok(clienteService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> criar(@RequestBody ClienteRequestDTO clienteDTO) {
        return ResponseEntity.ok(clienteService.save(clienteDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizar(@PathVariable Long id, @RequestBody ClienteRequestDTO clienteDTO) {
        return ResponseEntity.ok(clienteService.update(id, clienteDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}