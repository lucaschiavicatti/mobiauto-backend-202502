package com.mobiauto.backend.controller;

import com.mobiauto.backend.controller.swagger.SwaggerOportunidadeController;
import com.mobiauto.backend.dto.OportunidadeRequestDTO;
import com.mobiauto.backend.dto.OportunidadeResponseDTO;
import com.mobiauto.backend.service.OportunidadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/oportunidades")
public class OportunidadeController implements SwaggerOportunidadeController {
    private final OportunidadeService oportunidadeService;

    public OportunidadeController(OportunidadeService oportunidadeService) {
        this.oportunidadeService = oportunidadeService;
    }

    @GetMapping
    public ResponseEntity<List<OportunidadeResponseDTO>> listar() {
        return ResponseEntity.ok(oportunidadeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OportunidadeResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(oportunidadeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<OportunidadeResponseDTO> criar(@RequestBody OportunidadeRequestDTO oportunidadeDTO) {
        return ResponseEntity.ok(oportunidadeService.save(oportunidadeDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OportunidadeResponseDTO> atualizar(@PathVariable Long id, @RequestBody OportunidadeRequestDTO oportunidadeDTO) {
        return ResponseEntity.ok(oportunidadeService.update(id, oportunidadeDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        oportunidadeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}