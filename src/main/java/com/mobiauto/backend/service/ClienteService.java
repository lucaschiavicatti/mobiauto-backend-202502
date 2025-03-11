package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.ClienteRequestDTO;
import com.mobiauto.backend.dto.ClienteResponseDTO;
import com.mobiauto.backend.mapper.ClienteMapper;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Cliente;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.Usuario;
import com.mobiauto.backend.repository.ClienteRepository;
import com.mobiauto.backend.repository.RevendaRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final RevendaRepository revendaRepository;
    private final UsuarioService usuarioService;
    private final ClienteMapper clienteMapper;

    public List<ClienteResponseDTO> findAll() {
        Usuario usuarioLogado = getUsuarioLogado();
        if (usuarioLogado.getCargo() == Cargo.ADMINISTRADOR) {
            return clienteRepository.findAll().stream()
                    .map(clienteMapper::toResponseDTO)
                    .toList();
        }
        return clienteRepository.findAllByRevenda_Id(usuarioLogado.getRevenda().getId()).stream()
                .map(clienteMapper::toResponseDTO)
                .toList();
    }

    public ClienteResponseDTO findById(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cliente não encontrado"));
        checkRevendaAccess(usuarioLogado, cliente.getRevenda().getId());
        return clienteMapper.toResponseDTO(cliente);
    }

    public ClienteResponseDTO save(ClienteRequestDTO dto) {
        Usuario usuarioLogado = getUsuarioLogado();
        checkRevendaAccess(usuarioLogado, dto.getRevendaId());

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
        cliente = clienteRepository.save(cliente);
        return clienteMapper.toResponseDTO(cliente);
    }

    public ClienteResponseDTO update(Long id, ClienteRequestDTO dto) {
        Usuario usuarioLogado = getUsuarioLogado();
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cliente não encontrado"));
        checkRevendaAccess(usuarioLogado, cliente.getRevenda().getId());

        if (!cliente.getEmail().equals(dto.getEmail()) && clienteRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(CONFLICT, "Email já cadastrado: " + dto.getEmail());
        }
        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));

        cliente.setNome(dto.getNome());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefone(dto.getTelefone());
        cliente.setRevenda(revenda);
        cliente = clienteRepository.save(cliente);
        return clienteMapper.toResponseDTO(cliente);
    }

    public void delete(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cliente não encontrado"));
        checkRevendaAccess(usuarioLogado, cliente.getRevenda().getId());
        clienteRepository.deleteById(id);
    }

    private void checkRevendaAccess(Usuario usuarioLogado, Long revendaId) {
        if (usuarioLogado.getCargo() != Cargo.ADMINISTRADOR && !usuarioLogado.getRevenda().getId().equals(revendaId)) {
            throw new ResponseStatusException(FORBIDDEN, "Você só pode gerenciar clientes da sua revenda");
        }
    }

    private Usuario getUsuarioLogado() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return usuarioService.getUsuarioLogado();
    }
}