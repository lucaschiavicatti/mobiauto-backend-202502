package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.OportunidadeRequestDTO;
import com.mobiauto.backend.dto.OportunidadeResponseDTO;
import com.mobiauto.backend.model.*;
import com.mobiauto.backend.repository.ClienteRepository;
import com.mobiauto.backend.repository.OportunidadeRepository;
import com.mobiauto.backend.repository.RevendaRepository;
import com.mobiauto.backend.repository.VeiculoRepository;
import com.mobiauto.backend.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class OportunidadeService {
    private final OportunidadeRepository oportunidadeRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final RevendaRepository revendaRepository;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    public OportunidadeService(OportunidadeRepository oportunidadeRepository, ClienteRepository clienteRepository,
                               VeiculoRepository veiculoRepository, RevendaRepository revendaRepository,
                               UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
        this.oportunidadeRepository = oportunidadeRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.revendaRepository = revendaRepository;
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    public List<OportunidadeResponseDTO> findAll() {
        Usuario usuarioLogado = getUsuarioLogado();
        if (usuarioLogado.getCargo() == Cargo.ADMINISTRADOR) {
            return oportunidadeRepository.findAll().stream()
                    .map(this::toResponseDTO)
                    .toList();
        }
        return oportunidadeRepository.findAllByRevenda_Id(usuarioLogado.getRevenda().getId()).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public OportunidadeResponseDTO findById(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();
        Oportunidade oportunidade = oportunidadeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade não encontrada"));
        checkRevendaAccess(usuarioLogado, oportunidade.getRevenda().getId());
        return toResponseDTO(oportunidade);
    }

    public OportunidadeResponseDTO save(OportunidadeRequestDTO dto) {
        Usuario usuarioLogado = getUsuarioLogado();
        checkRevendaAccess(usuarioLogado, dto.getRevendaId());

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado: " + dto.getClienteId()));
        Veiculo veiculo = veiculoRepository.findById(dto.getVeiculoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado: " + dto.getVeiculoId()));
        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));

        Usuario usuarioResponsavel;
        LocalDateTime agora = LocalDateTime.now();
        if (dto.getUsuarioId() != null) {
            usuarioResponsavel = usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado: " + dto.getUsuarioId()));
            if (!usuarioResponsavel.getRevenda().getId().equals(dto.getRevendaId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário deve pertencer à mesma revenda");
            }
            usuarioResponsavel.setDataUltimaAtribuicao(agora);
            usuarioRepository.save(usuarioResponsavel);
        } else {
            usuarioResponsavel = distribuirParaAssistente(revenda);
            usuarioResponsavel.setDataUltimaAtribuicao(agora);
            usuarioRepository.save(usuarioResponsavel);
        }

        StatusOportunidade status = StatusOportunidade.valueOf(dto.getStatus());
        if (status == StatusOportunidade.CONCLUIDO && (dto.getMotivoConclusao() == null || dto.getMotivoConclusao().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Motivo de conclusão é obrigatório para status CONCLUIDO");
        }

        Oportunidade oportunidade = new Oportunidade();
        oportunidade.setCliente(cliente);
        oportunidade.setVeiculo(veiculo);
        oportunidade.setUsuario(usuarioResponsavel);
        oportunidade.setRevenda(revenda);
        oportunidade.setStatus(status);
        oportunidade.setMotivoConclusao(status == StatusOportunidade.CONCLUIDO ? dto.getMotivoConclusao() : null);
        oportunidade.setDataAtribuicao(agora);
        oportunidade.setDataConclusao(status == StatusOportunidade.CONCLUIDO ? agora : null);

        oportunidade = oportunidadeRepository.save(oportunidade);
        return toResponseDTO(oportunidade);
    }

    public OportunidadeResponseDTO update(Long id, OportunidadeRequestDTO dto) {
        Usuario usuarioLogado = getUsuarioLogado();
        Oportunidade oportunidade = oportunidadeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade não encontrada"));
        checkEditPermission(usuarioLogado, oportunidade);

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado: " + dto.getClienteId()));
        Veiculo veiculo = veiculoRepository.findById(dto.getVeiculoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado: " + dto.getVeiculoId()));
        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));

        Usuario usuarioResponsavel = null;
        LocalDateTime agora = LocalDateTime.now();
        if (dto.getUsuarioId() != null) {
            usuarioResponsavel = usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado: " + dto.getUsuarioId()));
            if (!usuarioResponsavel.getRevenda().getId().equals(dto.getRevendaId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário deve pertencer à mesma revenda");
            }
            usuarioResponsavel.setDataUltimaAtribuicao(agora);
            usuarioRepository.save(usuarioResponsavel);
        }

        StatusOportunidade novoStatus = StatusOportunidade.valueOf(dto.getStatus());
        if (novoStatus == StatusOportunidade.CONCLUIDO && (dto.getMotivoConclusao() == null || dto.getMotivoConclusao().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Motivo de conclusão é obrigatório para status CONCLUIDO");
        }

        oportunidade.setCliente(cliente);
        oportunidade.setVeiculo(veiculo);
        if (usuarioResponsavel != null) {
            oportunidade.setUsuario(usuarioResponsavel);
        }
        oportunidade.setRevenda(revenda);
        oportunidade.setStatus(novoStatus);
        oportunidade.setMotivoConclusao(novoStatus == StatusOportunidade.CONCLUIDO ? dto.getMotivoConclusao() : null);
        if (novoStatus == StatusOportunidade.CONCLUIDO && oportunidade.getDataConclusao() == null) {
            oportunidade.setDataConclusao(agora);
        }

        oportunidade = oportunidadeRepository.save(oportunidade);
        return toResponseDTO(oportunidade);
    }

    public void delete(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();
        Oportunidade oportunidade = oportunidadeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade não encontrada"));
        checkRevendaAccess(usuarioLogado, oportunidade.getRevenda().getId());
        oportunidadeRepository.deleteById(id);
    }

    private void checkRevendaAccess(Usuario usuarioLogado, Long revendaId) {
        if (usuarioLogado.getCargo() != Cargo.ADMINISTRADOR && !usuarioLogado.getRevenda().getId().equals(revendaId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode gerenciar oportunidades da sua revenda");
        }
    }

    private void checkEditPermission(Usuario usuarioLogado, Oportunidade oportunidade) {
        if (usuarioLogado.getCargo() != Cargo.ADMINISTRADOR &&
                usuarioLogado.getCargo() != Cargo.PROPRIETARIO &&
                usuarioLogado.getCargo() != Cargo.GERENTE &&
                !usuarioLogado.getId().equals(oportunidade.getUsuario().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode editar suas próprias oportunidades");
        }
        checkRevendaAccess(usuarioLogado, oportunidade.getRevenda().getId());
    }

    private Usuario distribuirParaAssistente(Revenda revenda) {
        List<Usuario> assistentes = usuarioService.findAll().stream()
                .map(usuarioService::toUsuario)
                .filter(u -> u.getCargo() == Cargo.ASSISTENTE && u.getRevenda().getId().equals(revenda.getId()))
                .toList();

        if (assistentes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum assistente disponível na revenda");
        }

        return assistentes.stream()
                .min(Comparator
                        .comparingInt((Usuario u) -> oportunidadeRepository.findAllEmAtendimentoByUsuarioId(u.getId()).size())
                        .thenComparing(Usuario::getDataUltimaAtribuicao, Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao distribuir oportunidade"));
    }

    private Usuario getUsuarioLogado() {
        return (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private OportunidadeResponseDTO toResponseDTO(Oportunidade oportunidade) {
        return new OportunidadeResponseDTO(
                oportunidade.getId(),
                oportunidade.getCliente().getId(),
                oportunidade.getVeiculo().getId(),
                oportunidade.getUsuario().getId(),
                oportunidade.getRevenda().getId(),
                oportunidade.getStatus().name(),
                oportunidade.getMotivoConclusao(),
                oportunidade.getDataAtribuicao(),
                oportunidade.getDataConclusao()
        );
    }
}