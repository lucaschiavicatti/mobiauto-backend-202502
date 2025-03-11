package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.RevendaRequestDTO;
import com.mobiauto.backend.dto.RevendaResponseDTO;
import com.mobiauto.backend.mapper.RevendaMapper;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.Usuario;
import com.mobiauto.backend.repository.RevendaRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@AllArgsConstructor
@Service
public class RevendaService {
    private final RevendaRepository revendaRepository;
    private final RevendaMapper revendaMapper;

    public List<RevendaResponseDTO> findAll() {
        Usuario usuarioLogado = getUsuarioLogado();
        if (usuarioLogado.getCargo() == Cargo.ADMINISTRADOR) {
            return revendaRepository.findAll().stream().map(revendaMapper::toResponseDTO).toList();
        }
        return List.of(revendaMapper.toResponseDTO(revendaRepository.findById(usuarioLogado.getRevenda().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Revenda não encontrada"))));
    }

    public RevendaResponseDTO findById(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();
        Revenda revenda = revendaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Revenda não encontrada"));
        if (usuarioLogado.getCargo() != Cargo.ADMINISTRADOR && !usuarioLogado.getRevenda().getId().equals(revenda.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode acessar sua própria revenda");
        }
        return revendaMapper.toResponseDTO(revenda);
    }

    public RevendaResponseDTO save(RevendaRequestDTO dto) {
        Usuario usuarioLogado = getUsuarioLogado();
        if (usuarioLogado.getCargo() != Cargo.ADMINISTRADOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas administradores podem criar revendas");
        }
        if (revendaRepository.existsByCnpj(dto.getCnpj())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CNPJ já cadastrado");
        }
        Revenda revenda = new Revenda();
        revenda.setCnpj(dto.getCnpj());
        revenda.setNomeSocial(dto.getNomeSocial());
        return revendaMapper.toResponseDTO(revendaRepository.save(revenda));
    }

    public RevendaResponseDTO update(Long id, RevendaRequestDTO dto) {
        Usuario usuarioLogado = getUsuarioLogado();
        if (usuarioLogado.getCargo() != Cargo.ADMINISTRADOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas administradores podem atualizar revendas");
        }
        Revenda revenda = revendaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Revenda não encontrada"));
        if (!revenda.getCnpj().equals(dto.getCnpj()) && revendaRepository.existsByCnpj(dto.getCnpj())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CNPJ já cadastrado");
        }
        revenda.setCnpj(dto.getCnpj());
        revenda.setNomeSocial(dto.getNomeSocial());
        return revendaMapper.toResponseDTO(revendaRepository.save(revenda));
    }

    public void delete(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();
        if (usuarioLogado.getCargo() != Cargo.ADMINISTRADOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas administradores podem excluir revendas");
        }
        Revenda revenda = revendaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Revenda não encontrada"));
        revendaRepository.deleteById(id);
    }

    private Usuario getUsuarioLogado() {
        return (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}