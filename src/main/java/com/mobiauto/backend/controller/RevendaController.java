package com.mobiauto.backend.controller;

import com.mobiauto.backend.dto.RevendaRequestDTO;
import com.mobiauto.backend.dto.RevendaResponseDTO;
import com.mobiauto.backend.service.RevendaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/revendas")
public class RevendaController {
    private final RevendaService revendaService;

    public RevendaController(RevendaService revendaService) {
        this.revendaService = revendaService;
    }

    @GetMapping
    public ResponseEntity<List<RevendaResponseDTO>> listarTodos() {
        return ResponseEntity.ok(revendaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RevendaResponseDTO> buscarPorId(@PathVariable Long id) {
        return revendaService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RevendaResponseDTO> criar(@RequestBody RevendaRequestDTO revendaDTO) {
        return ResponseEntity.ok(revendaService.save(revendaDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        revendaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}