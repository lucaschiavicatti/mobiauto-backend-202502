package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.UsuarioRequestDTO;
import com.mobiauto.backend.dto.UsuarioResponseDTO;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.Usuario;
import com.mobiauto.backend.repository.RevendaRepository;
import com.mobiauto.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final RevendaRepository revendaRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository usuarioRepository, RevendaRepository revendaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.revendaRepository = revendaRepository;
    }

    public List<UsuarioResponseDTO> findAll() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public Optional<UsuarioResponseDTO> findById(Long id) {
        return usuarioRepository.findById(id).map(this::toResponseDTO);
    }

    public Optional<UsuarioResponseDTO> findByEmail(String email) {
        return usuarioRepository.findByEmail(email).map(this::toResponseDTO);
    }

    public UsuarioResponseDTO save(UsuarioRequestDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setCargo(dto.getCargo());
        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new RuntimeException("Revenda não encontrada"));
        usuario.setRevenda(revenda);
        return toResponseDTO(usuarioRepository.save(usuario));
    }

    public void delete(Long id) {
        usuarioRepository.deleteById(id);
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCargo().name()
        );
    }
}