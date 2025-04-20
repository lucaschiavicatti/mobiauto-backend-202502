package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.RevendaRequestDTO;
import com.mobiauto.backend.dto.RevendaResponseDTO;
import com.mobiauto.backend.mapper.RevendaMapper;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.repository.RevendaRepository;
import com.mobiauto.backend.utils.JwtAuthUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.mobiauto.backend.model.Cargo.ADMINISTRADOR;

@AllArgsConstructor
@Service
public class RevendaService {
    private final RevendaRepository revendaRepository;
    private final RevendaMapper revendaMapper;

    private static final String NOT_FOUND_MESSAGE = "Revenda não encontrada";

    public List<RevendaResponseDTO> findAll() {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        if (cargos.contains(ADMINISTRADOR)) {
            return revendaRepository.findAll().stream()
                    .map(revendaMapper::toResponseDTO)
                    .toList();
        }

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        return List.of(revendaMapper.toResponseDTO(
                revendaRepository.findById(revendaId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND_MESSAGE))
        ));
    }

    public RevendaResponseDTO findById(Long id) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        Revenda revenda = revendaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND_MESSAGE));

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        if (!cargos.contains(ADMINISTRADOR) && !revendaId.equals(revenda.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode acessar sua própria revenda");
        }

        return revendaMapper.toResponseDTO(revenda);
    }

    public RevendaResponseDTO save(RevendaRequestDTO dto) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        if (!cargos.contains(ADMINISTRADOR)) {
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
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        if (!cargos.contains(ADMINISTRADOR)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas administradores podem atualizar revendas");
        }

        Revenda revenda = revendaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND_MESSAGE));

        if (!revenda.getCnpj().equals(dto.getCnpj()) && revendaRepository.existsByCnpj(dto.getCnpj())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CNPJ já cadastrado");
        }

        revenda.setCnpj(dto.getCnpj());
        revenda.setNomeSocial(dto.getNomeSocial());
        return revendaMapper.toResponseDTO(revendaRepository.save(revenda));
    }

    public void delete(Long id) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        if (!cargos.contains(ADMINISTRADOR)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas administradores podem excluir revendas");
        }

        revendaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND_MESSAGE));
        revendaRepository.deleteById(id);
    }
}