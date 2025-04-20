package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.ClienteRequestDTO;
import com.mobiauto.backend.dto.ClienteResponseDTO;
import com.mobiauto.backend.mapper.ClienteMapper;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Cliente;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.repository.ClienteRepository;
import com.mobiauto.backend.repository.RevendaRepository;
import com.mobiauto.backend.utils.JwtAuthUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final RevendaRepository revendaRepository;
    private final ClienteMapper clienteMapper;

    public List<ClienteResponseDTO> findAll() {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        if (cargos.contains(Cargo.ADMINISTRADOR)) {
            return clienteRepository.findAll().stream()
                    .map(clienteMapper::toResponseDTO)
                    .toList();
        }

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        return clienteRepository.findAllByRevenda_Id(revendaId).stream()
                .map(clienteMapper::toResponseDTO)
                .toList();
    }

    public ClienteResponseDTO findById(Long id) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cliente não encontrado"));

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        checkRevendaAccess(cargos, revendaId, cliente.getRevenda().getId());

        return clienteMapper.toResponseDTO(cliente);
    }

    public ClienteResponseDTO save(ClienteRequestDTO dto) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        checkRevendaAccess(cargos, revendaId, dto.getRevendaId());

        if (clienteRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(CONFLICT, "Email já cadastrado: " + dto.getEmail());
        }

        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));

        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefone(dto.getTelefone());
        cliente.setRevenda(revenda);
        return clienteMapper.toResponseDTO(clienteRepository.save(cliente));
    }

    public ClienteResponseDTO update(Long id, ClienteRequestDTO dto) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cliente não encontrado"));

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        checkRevendaAccess(cargos, revendaId, cliente.getRevenda().getId());

        if (!cliente.getEmail().equals(dto.getEmail()) && clienteRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(CONFLICT, "Email já cadastrado: " + dto.getEmail());
        }

        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));

        cliente.setNome(dto.getNome());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefone(dto.getTelefone());
        cliente.setRevenda(revenda);
        return clienteMapper.toResponseDTO(clienteRepository.save(cliente));
    }

    public void delete(Long id) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cliente não encontrado"));

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        checkRevendaAccess(cargos, revendaId, cliente.getRevenda().getId());

        clienteRepository.deleteById(id);
    }

    private void checkRevendaAccess(List<Cargo> cargos, Long revendaIdUsuario, Long revendaIdRecurso) {
        if (!cargos.contains(Cargo.ADMINISTRADOR) && !revendaIdUsuario.equals(revendaIdRecurso)) {
            throw new ResponseStatusException(FORBIDDEN, "Você só pode gerenciar clientes da sua revenda");
        }
    }
}