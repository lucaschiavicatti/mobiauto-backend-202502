package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.UsuarioRequestDTO;
import com.mobiauto.backend.dto.UsuarioResponseDTO;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.Usuario;
import com.mobiauto.backend.repository.RevendaRepository;
import com.mobiauto.backend.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

import static com.mobiauto.backend.model.Cargo.*;
import static org.springframework.http.HttpStatus.*;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final RevendaRepository revendaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, RevendaRepository revendaRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.revendaRepository = revendaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioResponseDTO> findAll() {
        Usuario usuarioLogado = getUsuarioLogado();
        if (usuarioLogado.getCargo() == ADMINISTRADOR) {
            return usuarioRepository.findAll().stream()
                    .map(this::toResponseDTO)
                    .toList();
        }
        return usuarioRepository.findAllByRevendaId(usuarioLogado.getRevenda().getId()).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public UsuarioResponseDTO findById(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuário não encontrado"));
        checkRevendaAccess(usuarioLogado, usuario.getRevenda().getId());
        return toResponseDTO(usuario);
    }

    public UsuarioResponseDTO save(UsuarioRequestDTO dto) {
        Usuario usuarioLogado = getUsuarioLogado();
        checkCadastroPermission(usuarioLogado, dto.getRevendaId());

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado: " + dto.getEmail());
        }
        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setCargo(dto.getCargo());
        usuario.setRevenda(revenda);
        usuario = usuarioRepository.save(usuario);
        return toResponseDTO(usuario);
    }

    public UsuarioResponseDTO update(Long id, UsuarioRequestDTO dto) {
        Usuario usuarioLogado = getUsuarioLogado();
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuário não encontrado"));
        checkEdicaoPermission(usuarioLogado, usuario.getRevenda().getId());

        if (!usuario.getEmail().equals(dto.getEmail()) && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado: " + dto.getEmail());
        }
        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setCargo(dto.getCargo());
        usuario.setRevenda(revenda);
        usuario = usuarioRepository.save(usuario);
        return toResponseDTO(usuario);
    }

    public void delete(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuário não encontrado"));
        checkRevendaAccess(usuarioLogado, usuario.getRevenda().getId());
        usuarioRepository.deleteById(id);
    }

    private void checkCadastroPermission(Usuario usuarioLogado, Long revendaId) {
        Cargo cargo = usuarioLogado.getCargo();
        if (cargo != ADMINISTRADOR && cargo != PROPRIETARIO && cargo != GERENTE) {
            throw new ResponseStatusException(FORBIDDEN, "Apenas Administradores, Proprietários ou Gerentes podem cadastrar usuários");
        }
        if (cargo != ADMINISTRADOR && !usuarioLogado.getRevenda().getId().equals(revendaId)) {
            throw new ResponseStatusException(FORBIDDEN, "Você só pode cadastrar usuários na sua revenda");
        }
    }

    private void checkEdicaoPermission(Usuario usuarioLogado, Long revendaId) {
        Cargo cargo = usuarioLogado.getCargo();
        if (cargo != ADMINISTRADOR && cargo != PROPRIETARIO) {
            throw new ResponseStatusException(FORBIDDEN, "Apenas Administradores ou Proprietários podem editar usuários");
        }
        if (cargo == PROPRIETARIO && !usuarioLogado.getRevenda().getId().equals(revendaId)) {
            throw new ResponseStatusException(FORBIDDEN, "Você só pode editar usuários da sua revenda");
        }
    }

    private void checkRevendaAccess(Usuario usuarioLogado, Long revendaId) {
        if (usuarioLogado.getCargo() != ADMINISTRADOR && !usuarioLogado.getRevenda().getId().equals(revendaId)) {
            throw new ResponseStatusException(FORBIDDEN, "Você só pode acessar usuários da sua revenda");
        }
    }

    private Usuario getUsuarioLogado() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(INTERNAL_SERVER_ERROR, "Usuário logado não encontrado"));
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCargo(),
                usuario.getRevenda().getId()
        );
    }
}