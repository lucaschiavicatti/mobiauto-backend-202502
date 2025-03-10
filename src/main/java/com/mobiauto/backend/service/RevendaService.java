package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.RevendaRequestDTO;
import com.mobiauto.backend.dto.RevendaResponseDTO;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.repository.RevendaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class RevendaService {
    private final RevendaRepository revendaRepository;

    public RevendaService(RevendaRepository revendaRepository) {
        this.revendaRepository = revendaRepository;
    }

    public List<RevendaResponseDTO> findAll() {
        return revendaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public Optional<RevendaResponseDTO> findById(Long id) {
        return revendaRepository.findById(id).map(this::toResponseDTO);
    }

    public RevendaResponseDTO save(RevendaRequestDTO dto) {
        if (revendaRepository.existsByCnpj(dto.getCnpj())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CNPJ já cadastrado: " + dto.getCnpj());
        }
        Revenda revenda = new Revenda();
        revenda.setCnpj(dto.getCnpj());
        revenda.setNomeSocial(dto.getNomeSocial());
        return toResponseDTO(revendaRepository.save(revenda));
    }

    public void delete(Long id) {
        if (!revendaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Revenda não encontrada");
        }
        try {
            revendaRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Revenda não pode ser deletada pois está associada a usuários");
        }
    }

    private RevendaResponseDTO toResponseDTO(Revenda revenda) {
        return new RevendaResponseDTO(
                revenda.getId(),
                revenda.getCnpj(),
                revenda.getNomeSocial()
        );
    }
}