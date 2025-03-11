package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.VeiculoRequestDTO;
import com.mobiauto.backend.dto.VeiculoResponseDTO;
import com.mobiauto.backend.mapper.VeiculoMapper;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.Veiculo;
import com.mobiauto.backend.model.Usuario;
import com.mobiauto.backend.repository.RevendaRepository;
import com.mobiauto.backend.repository.VeiculoRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@AllArgsConstructor
@Service
public class VeiculoService {
    private final VeiculoRepository veiculoRepository;
    private final RevendaRepository revendaRepository;
    private final VeiculoMapper veiculoMapper;

    public List<VeiculoResponseDTO> findAll() {
        Usuario usuarioLogado = getUsuarioLogado();
        if (usuarioLogado.getCargo() == Cargo.ADMINISTRADOR) {
            return veiculoRepository.findAll().stream()
                    .map(veiculoMapper::toResponseDTO)
                    .toList();
        }
        return veiculoRepository.findAllByRevenda_Id(usuarioLogado.getRevenda().getId()).stream()
                .map(veiculoMapper::toResponseDTO)
                .toList();
    }

    public VeiculoResponseDTO findById(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado"));
        if (usuarioLogado.getCargo() != Cargo.ADMINISTRADOR && !usuarioLogado.getRevenda().getId().equals(veiculo.getRevenda().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode acessar veículos da sua revenda");
        }
        return veiculoMapper.toResponseDTO(veiculo);
    }

    public VeiculoResponseDTO save(VeiculoRequestDTO dto) {
        Usuario usuarioLogado = getUsuarioLogado();
        if (usuarioLogado.getCargo() != Cargo.ADMINISTRADOR && !usuarioLogado.getRevenda().getId().equals(dto.getRevendaId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode criar veículos na sua revenda");
        }
        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));
        Veiculo veiculo = new Veiculo();
        veiculo.setMarca(dto.getMarca());
        veiculo.setModelo(dto.getModelo());
        veiculo.setVersao(dto.getVersao());
        veiculo.setAnoModelo(dto.getAnoModelo());
        veiculo.setRevenda(revenda);
        return veiculoMapper.toResponseDTO(veiculoRepository.save(veiculo));
    }

    public VeiculoResponseDTO update(Long id, VeiculoRequestDTO dto) {
        Usuario usuarioLogado = getUsuarioLogado();
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado"));
        if (usuarioLogado.getCargo() != Cargo.ADMINISTRADOR && !usuarioLogado.getRevenda().getId().equals(veiculo.getRevenda().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode atualizar veículos da sua revenda");
        }
        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));
        veiculo.setMarca(dto.getMarca());
        veiculo.setModelo(dto.getModelo());
        veiculo.setVersao(dto.getVersao());
        veiculo.setAnoModelo(dto.getAnoModelo());
        veiculo.setRevenda(revenda);
        return veiculoMapper.toResponseDTO(veiculoRepository.save(veiculo));
    }

    public void delete(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado"));
        if (usuarioLogado.getCargo() != Cargo.ADMINISTRADOR && !usuarioLogado.getRevenda().getId().equals(veiculo.getRevenda().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode excluir veículos da sua revenda");
        }
        veiculoRepository.deleteById(id);
    }

    private Usuario getUsuarioLogado() {
        return (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}