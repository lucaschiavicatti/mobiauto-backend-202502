package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.OportunidadeRequestDTO;
import com.mobiauto.backend.dto.OportunidadeResponseDTO;
import com.mobiauto.backend.mapper.OportunidadeMapper;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Cliente;
import com.mobiauto.backend.model.Oportunidade;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.StatusOportunidade;
import com.mobiauto.backend.model.Usuario;
import com.mobiauto.backend.model.Veiculo;
import com.mobiauto.backend.repository.ClienteRepository;
import com.mobiauto.backend.repository.OportunidadeRepository;
import com.mobiauto.backend.repository.RevendaRepository;
import com.mobiauto.backend.repository.UsuarioRepository;
import com.mobiauto.backend.repository.VeiculoRepository;
import com.mobiauto.backend.utils.JwtAuthUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static com.mobiauto.backend.model.Cargo.*;
import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
@Service
public class OportunidadeService {
    private final OportunidadeRepository oportunidadeRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final RevendaRepository revendaRepository;
    private final UsuarioRepository usuarioRepository;
    private final OportunidadeMapper oportunidadeMapper;

    public List<OportunidadeResponseDTO> findAll() {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        if (cargos.contains(ADMINISTRADOR)) {
            return oportunidadeRepository.findAll().stream()
                    .map(oportunidadeMapper::toResponseDTO)
                    .toList();
        }

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        return oportunidadeRepository.findAllByRevenda_Id(revendaId).stream()
                .map(oportunidadeMapper::toResponseDTO)
                .toList();
    }

    public OportunidadeResponseDTO findById(Long id) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        Oportunidade oportunidade = oportunidadeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Oportunidade não encontrada"));

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        if (!cargos.contains(ADMINISTRADOR) && !revendaId.equals(oportunidade.getRevenda().getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Você só pode acessar oportunidades da sua revenda");
        }

        return oportunidadeMapper.toResponseDTO(oportunidade);
    }

    public OportunidadeResponseDTO save(OportunidadeRequestDTO dto) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        if (!cargos.contains(ADMINISTRADOR) && !revendaId.equals(dto.getRevendaId())) {
            throw new ResponseStatusException(FORBIDDEN, "Você só pode criar oportunidades na sua revenda");
        }

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cliente não encontrado: " + dto.getClienteId()));
        Veiculo veiculo = veiculoRepository.findById(dto.getVeiculoId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Veículo não encontrado: " + dto.getVeiculoId()));
        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));

        Usuario usuarioResponsavel;
        LocalDateTime agora = LocalDateTime.now();
        if (dto.getUsuarioId() != null) {
            usuarioResponsavel = usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuário não encontrado: " + dto.getUsuarioId()));
            if (!usuarioResponsavel.getRevenda().getId().equals(dto.getRevendaId())) {
                throw new ResponseStatusException(BAD_REQUEST, "Usuário deve pertencer à mesma revenda");
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
            throw new ResponseStatusException(BAD_REQUEST, "Motivo de conclusão é obrigatório para status CONCLUIDO");
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

        return oportunidadeMapper.toResponseDTO(oportunidadeRepository.save(oportunidade));
    }

    public OportunidadeResponseDTO update(Long id, OportunidadeRequestDTO dto) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        Oportunidade oportunidade = oportunidadeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Oportunidade não encontrada"));

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        if (!cargos.contains(ADMINISTRADOR) && !revendaId.equals(oportunidade.getRevenda().getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Você só pode editar oportunidades da sua revenda");
        }

        Long usuarioId = Long.valueOf(jwt.getSubject());
        if (!cargos.contains(ADMINISTRADOR) && !cargos.contains(PROPRIETARIO) && !cargos.contains(GERENTE) && !usuarioId.equals(oportunidade.getUsuario().getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Você só pode editar suas próprias oportunidades");
        }

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cliente não encontrado: " + dto.getClienteId()));
        Veiculo veiculo = veiculoRepository.findById(dto.getVeiculoId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Veículo não encontrado: " + dto.getVeiculoId()));
        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));

        Usuario usuarioResponsavel = null;
        LocalDateTime agora = LocalDateTime.now();
        if (dto.getUsuarioId() != null) {
            usuarioResponsavel = usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuário não encontrado: " + dto.getUsuarioId()));
            if (!usuarioResponsavel.getRevenda().getId().equals(dto.getRevendaId())) {
                throw new ResponseStatusException(BAD_REQUEST, "Usuário deve pertencer à mesma revenda");
            }
            usuarioResponsavel.setDataUltimaAtribuicao(agora);
            usuarioRepository.save(usuarioResponsavel);
        }

        StatusOportunidade novoStatus = StatusOportunidade.valueOf(dto.getStatus());
        if (novoStatus == StatusOportunidade.CONCLUIDO && (dto.getMotivoConclusao() == null || dto.getMotivoConclusao().isBlank())) {
            throw new ResponseStatusException(BAD_REQUEST, "Motivo de conclusão é obrigatório para status CONCLUIDO");
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

        return oportunidadeMapper.toResponseDTO(oportunidadeRepository.save(oportunidade));
    }

    public void delete(Long id) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        Oportunidade oportunidade = oportunidadeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Oportunidade não encontrada"));

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        if (!cargos.contains(ADMINISTRADOR) && !revendaId.equals(oportunidade.getRevenda().getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Você só pode excluir oportunidades da sua revenda");
        }

        oportunidadeRepository.deleteById(id);
    }

    private Usuario distribuirParaAssistente(Revenda revenda) {
        List<Usuario> assistentes = usuarioRepository.findByCargoAndRevendaId(ASSISTENTE, revenda.getId());

        if (assistentes.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Nenhum assistente disponível na revenda");
        }

        return assistentes.stream()
                .min(Comparator
                        .comparingInt((Usuario u) -> oportunidadeRepository.findAllEmAtendimentoByUsuarioId(u.getId()).size())
                        .thenComparing(Usuario::getDataUltimaAtribuicao, Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElseThrow(() -> new ResponseStatusException(INTERNAL_SERVER_ERROR, "Erro ao distribuir oportunidade"));
    }
}