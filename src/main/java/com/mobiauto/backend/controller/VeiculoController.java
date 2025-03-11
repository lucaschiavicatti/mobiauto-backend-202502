package com.mobiauto.backend.controller;

import com.mobiauto.backend.controller.swagger.SwaggerVeiculoController;
import com.mobiauto.backend.dto.VeiculoRequestDTO;
import com.mobiauto.backend.dto.VeiculoResponseDTO;
import com.mobiauto.backend.service.VeiculoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/veiculos")
public class VeiculoController implements SwaggerVeiculoController {
    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @GetMapping
    public ResponseEntity<List<VeiculoResponseDTO>> listar() {
        return ResponseEntity.ok(veiculoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(veiculoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<VeiculoResponseDTO> criar(@RequestBody VeiculoRequestDTO veiculoDTO) {
        return ResponseEntity.ok(veiculoService.save(veiculoDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponseDTO> atualizar(@PathVariable Long id, @RequestBody VeiculoRequestDTO veiculoDTO) {
        return ResponseEntity.ok(veiculoService.update(id, veiculoDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        veiculoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}