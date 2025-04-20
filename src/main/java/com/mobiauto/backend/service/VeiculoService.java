package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.VeiculoRequestDTO;
import com.mobiauto.backend.dto.VeiculoResponseDTO;
import com.mobiauto.backend.mapper.VeiculoMapper;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.Veiculo;
import com.mobiauto.backend.repository.RevendaRepository;
import com.mobiauto.backend.repository.VeiculoRepository;
import com.mobiauto.backend.utils.JwtAuthUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.mobiauto.backend.model.Cargo.ADMINISTRADOR;
import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
@Service
public class VeiculoService {
    private final VeiculoRepository veiculoRepository;
    private final RevendaRepository revendaRepository;
    private final VeiculoMapper veiculoMapper;

    public List<VeiculoResponseDTO> findAll() {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        if (cargos.contains(ADMINISTRADOR)) {
            return veiculoRepository.findAll().stream()
                    .map(veiculoMapper::toResponseDTO)
                    .toList();
        }

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        return veiculoRepository.findAllByRevenda_Id(revendaId).stream()
                .map(veiculoMapper::toResponseDTO)
                .toList();
    }

    public VeiculoResponseDTO findById(Long id) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Veículo não encontrado"));

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        if (!cargos.contains(ADMINISTRADOR) && !revendaId.equals(veiculo.getRevenda().getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Você só pode acessar veículos da sua revenda");
        }

        return veiculoMapper.toResponseDTO(veiculo);
    }

    public VeiculoResponseDTO save(VeiculoRequestDTO dto) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        if (!cargos.contains(ADMINISTRADOR) && !revendaId.equals(dto.getRevendaId())) {
            throw new ResponseStatusException(FORBIDDEN, "Você só pode criar veículos na sua revenda");
        }

        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));

        Veiculo veiculo = new Veiculo();
        veiculo.setMarca(dto.getMarca());
        veiculo.setModelo(dto.getModelo());
        veiculo.setVersao(dto.getVersao());
        veiculo.setAnoModelo(dto.getAnoModelo());
        veiculo.setRevenda(revenda);

        return veiculoMapper.toResponseDTO(veiculoRepository.save(veiculo));
    }

    public VeiculoResponseDTO update(Long id, VeiculoRequestDTO dto) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Veículo não encontrado"));

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        if (!cargos.contains(ADMINISTRADOR) && !revendaId.equals(veiculo.getRevenda().getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Você só pode atualizar veículos da sua revenda");
        }

        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));

        veiculo.setMarca(dto.getMarca());
        veiculo.setModelo(dto.getModelo());
        veiculo.setVersao(dto.getVersao());
        veiculo.setAnoModelo(dto.getAnoModelo());
        veiculo.setRevenda(revenda);

        return veiculoMapper.toResponseDTO(veiculoRepository.save(veiculo));
    }

    public void delete(Long id) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Veículo não encontrado"));

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        if (!cargos.contains(ADMINISTRADOR) && !revendaId.equals(veiculo.getRevenda().getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Você só pode excluir veículos da sua revenda");
        }

        veiculoRepository.deleteById(id);
    }
}