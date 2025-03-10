package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.VeiculoRequestDTO;
import com.mobiauto.backend.dto.VeiculoResponseDTO;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.Veiculo;
import com.mobiauto.backend.model.Usuario;
import com.mobiauto.backend.repository.RevendaRepository;
import com.mobiauto.backend.repository.VeiculoRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

import static com.mobiauto.backend.model.Cargo.ADMINISTRADOR;

@Service
public class VeiculoService {
    private final VeiculoRepository veiculoRepository;
    private final RevendaRepository revendaRepository;
    private final UsuarioService usuarioService;

    public VeiculoService(VeiculoRepository veiculoRepository, RevendaRepository revendaRepository, UsuarioService usuarioService) {
        this.veiculoRepository = veiculoRepository;
        this.revendaRepository = revendaRepository;
        this.usuarioService = usuarioService;
    }

    public List<VeiculoResponseDTO> findAll() {
        Usuario usuarioLogado = getUsuarioLogado();
        if (usuarioLogado.getCargo() == ADMINISTRADOR) {
            return veiculoRepository.findAll().stream()
                    .map(this::toResponseDTO)
                    .toList();
        }
        return veiculoRepository.findAllByRevenda_Id(usuarioLogado.getRevenda().getId()).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public VeiculoResponseDTO findById(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado"));
        checkRevendaAccess(usuarioLogado, veiculo.getRevenda().getId());
        return toResponseDTO(veiculo);
    }

    public VeiculoResponseDTO save(VeiculoRequestDTO dto) {
        Usuario usuarioLogado = getUsuarioLogado();
        checkRevendaAccess(usuarioLogado, dto.getRevendaId());

        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));

        Veiculo veiculo = new Veiculo();
        veiculo.setMarca(dto.getMarca());
        veiculo.setModelo(dto.getModelo());
        veiculo.setVersao(dto.getVersao());
        veiculo.setAnoModelo(dto.getAnoModelo());
        veiculo.setRevenda(revenda);
        veiculo = veiculoRepository.save(veiculo);
        return toResponseDTO(veiculo);
    }

    public VeiculoResponseDTO update(Long id, VeiculoRequestDTO dto) {
        Usuario usuarioLogado = getUsuarioLogado();
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado"));
        checkRevendaAccess(usuarioLogado, veiculo.getRevenda().getId());

        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));

        veiculo.setMarca(dto.getMarca());
        veiculo.setModelo(dto.getModelo());
        veiculo.setVersao(dto.getVersao());
        veiculo.setAnoModelo(dto.getAnoModelo());
        veiculo.setRevenda(revenda);
        veiculo = veiculoRepository.save(veiculo);
        return toResponseDTO(veiculo);
    }

    public void delete(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado"));
        checkRevendaAccess(usuarioLogado, veiculo.getRevenda().getId());
        veiculoRepository.deleteById(id);
    }

    private void checkRevendaAccess(Usuario usuarioLogado, Long revendaId) {
        if (usuarioLogado.getCargo() != ADMINISTRADOR && !usuarioLogado.getRevenda().getId().equals(revendaId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode gerenciar veículos da sua revenda");
        }
    }

    private Usuario getUsuarioLogado() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return usuarioService.getUsuarioLogado();
    }

    private VeiculoResponseDTO toResponseDTO(Veiculo veiculo) {
        return new VeiculoResponseDTO(
                veiculo.getId(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getVersao(),
                veiculo.getAnoModelo(),
                veiculo.getRevenda().getId()
        );
    }
}