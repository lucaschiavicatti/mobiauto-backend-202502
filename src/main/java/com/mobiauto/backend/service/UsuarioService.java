package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.UsuarioRequestDTO;
import com.mobiauto.backend.dto.UsuarioResponseDTO;
import com.mobiauto.backend.mapper.UsuarioMapper;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.Usuario;
import com.mobiauto.backend.repository.RevendaRepository;
import com.mobiauto.backend.repository.UsuarioRepository;
import com.mobiauto.backend.utils.JwtAuthUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final RevendaRepository revendaRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    public List<UsuarioResponseDTO> findAll() {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        if (cargos.contains(Cargo.ADMINISTRADOR)) {
            return usuarioRepository.findAll().stream()
                    .map(usuarioMapper::toResponseDTO)
                    .toList();
        }

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        return usuarioRepository.findAllByRevenda_Id(revendaId).stream()
                .map(usuarioMapper::toResponseDTO)
                .toList();
    }

    public UsuarioResponseDTO findById(Long id) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuário não encontrado"));

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        if (!cargos.contains(Cargo.ADMINISTRADOR) && !revendaId.equals(usuario.getRevenda().getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Você só pode acessar usuários da sua revenda");
        }

        return usuarioMapper.toResponseDTO(usuario);
    }

    public UsuarioResponseDTO findByEmail(String email) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuário não encontrado com o e-mail: " + email));

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        if (!cargos.contains(Cargo.ADMINISTRADOR) && !revendaId.equals(usuario.getRevenda().getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Você só pode acessar usuários da sua revenda");
        }

        return usuarioMapper.toResponseDTO(usuario);
    }

    public UsuarioResponseDTO save(UsuarioRequestDTO dto) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        if (!cargos.contains(Cargo.ADMINISTRADOR) && !cargos.contains(Cargo.PROPRIETARIO) && !cargos.contains(Cargo.GERENTE)) {
            throw new ResponseStatusException(FORBIDDEN, "Apenas administradores, proprietários ou gerentes podem cadastrar usuários");
        }

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        if ((cargos.contains(Cargo.PROPRIETARIO) || cargos.contains(Cargo.GERENTE)) && !revendaId.equals(dto.getRevendaId())) {
            throw new ResponseStatusException(FORBIDDEN, "Proprietários e gerentes só podem cadastrar usuários em sua própria revenda");
        }

        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(BAD_REQUEST, "E-mail já cadastrado");
        }

        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setCargo(dto.getCargo());
        usuario.setRevenda(revenda);
        usuario.setDataUltimaAtribuicao(null);

        return usuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO update(Long id, UsuarioRequestDTO dto) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        if (!cargos.contains(Cargo.ADMINISTRADOR) && !cargos.contains(Cargo.PROPRIETARIO)) {
            throw new ResponseStatusException(FORBIDDEN, "Apenas administradores ou proprietários podem editar perfis");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuário não encontrado"));

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        if (cargos.contains(Cargo.PROPRIETARIO) && !revendaId.equals(usuario.getRevenda().getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Proprietários só podem editar usuários da sua própria revenda");
        }

        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        usuario.setCargo(dto.getCargo());
        usuario.setRevenda(revenda);

        return usuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    public void delete(Long id) {
        Jwt jwt = JwtAuthUtil.getJwt();
        List<Cargo> cargos = JwtAuthUtil.getCargosFromJwt(jwt);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuário não encontrado"));

        Long revendaId = Long.valueOf(jwt.getClaimAsString("revendaId"));
        if (!cargos.contains(Cargo.ADMINISTRADOR) && !revendaId.equals(usuario.getRevenda().getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Você só pode excluir usuários da sua revenda");
        }

        usuarioRepository.deleteById(id);
    }

    public Usuario toUsuario(UsuarioResponseDTO dto) {
        return usuarioRepository.findById(dto.id())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuário não encontrado: " + dto.id()));
    }
}