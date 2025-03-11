package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.UsuarioRequestDTO;
import com.mobiauto.backend.dto.UsuarioResponseDTO;
import com.mobiauto.backend.mapper.UsuarioMapper;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.Usuario;
import com.mobiauto.backend.repository.RevendaRepository;
import com.mobiauto.backend.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@AllArgsConstructor
@Service
public class UsuarioService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    private final RevendaRepository revendaRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }

    public List<UsuarioResponseDTO> findAll() {
        Usuario usuarioLogado = getUsuarioLogado();
        if (usuarioLogado.getCargo() == Cargo.ADMINISTRADOR) {
            return usuarioRepository.findAll().stream()
                    .map(usuarioMapper::toResponseDTO)
                    .toList();
        }
        return usuarioRepository.findAllByRevenda_Id(usuarioLogado.getRevenda().getId()).stream()
                .map(usuarioMapper::toResponseDTO)
                .toList();
    }

    public UsuarioResponseDTO findById(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        if (usuarioLogado.getCargo() != Cargo.ADMINISTRADOR && !usuarioLogado.getRevenda().getId().equals(usuario.getRevenda().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode acessar usuários da sua revenda");
        }
        return usuarioMapper.toResponseDTO(usuario);
    }

    public UsuarioResponseDTO findByEmail(String email) {
        Usuario usuarioLogado = getUsuarioLogado();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado com o e-mail: " + email));
        if (usuarioLogado.getCargo() != Cargo.ADMINISTRADOR && !usuarioLogado.getRevenda().getId().equals(usuario.getRevenda().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode acessar usuários da sua revenda");
        }
        return usuarioMapper.toResponseDTO(usuario);
    }

    public UsuarioResponseDTO save(UsuarioRequestDTO dto) {
        Usuario usuarioLogado = getUsuarioLogado();
        Cargo cargoLogado = usuarioLogado.getCargo();

        if (cargoLogado != Cargo.ADMINISTRADOR && cargoLogado != Cargo.PROPRIETARIO && cargoLogado != Cargo.GERENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas administradores, proprietários ou gerentes podem cadastrar usuários");
        }

        if ((cargoLogado == Cargo.PROPRIETARIO || cargoLogado == Cargo.GERENTE) && !usuarioLogado.getRevenda().getId().equals(dto.getRevendaId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Proprietários e gerentes só podem cadastrar usuários em sua própria revenda");
        }

        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail já cadastrado");
        }

        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setCargo((dto.getCargo()));
        usuario.setRevenda(revenda);
        usuario.setDataUltimaAtribuicao(null);

        usuario = usuarioRepository.save(usuario);
        return usuarioMapper.toResponseDTO(usuario);
    }

    public UsuarioResponseDTO update(Long id, UsuarioRequestDTO dto) {
        Usuario usuarioLogado = getUsuarioLogado();
        Cargo cargoLogado = usuarioLogado.getCargo();

        if (cargoLogado != Cargo.ADMINISTRADOR && cargoLogado != Cargo.PROPRIETARIO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas administradores ou proprietários podem editar perfis");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (cargoLogado == Cargo.PROPRIETARIO && !usuarioLogado.getRevenda().getId().equals(usuario.getRevenda().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Proprietários só podem editar usuários da sua própria revenda");
        }

        Revenda revenda = revendaRepository.findById(dto.getRevendaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Revenda não encontrada: " + dto.getRevendaId()));

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        usuario.setCargo((dto.getCargo()));
        usuario.setRevenda(revenda);

        usuario = usuarioRepository.save(usuario);
        return usuarioMapper.toResponseDTO(usuario);
    }

    public void delete(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();
        Cargo cargoLogado = usuarioLogado.getCargo();

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (cargoLogado != Cargo.ADMINISTRADOR && !usuarioLogado.getRevenda().getId().equals(usuario.getRevenda().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode excluir usuários da sua revenda");
        }

        usuarioRepository.deleteById(id);
    }

    public Usuario getUsuarioLogado() {
        return (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Usuario toUsuario(UsuarioResponseDTO dto) {
        return usuarioRepository.findById(dto.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado: " + dto.id()));
    }
}