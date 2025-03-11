package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.UsuarioRequestDTO;
import com.mobiauto.backend.dto.UsuarioResponseDTO;
import com.mobiauto.backend.mapper.UsuarioMapper;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.Usuario;
import com.mobiauto.backend.repository.RevendaRepository;
import com.mobiauto.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RevendaRepository revendaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioAdmin;
    private Usuario usuarioGerente;
    private Usuario usuarioProprietario;
    private Revenda revenda;

    @BeforeEach
    void setUp() {
        revenda = new Revenda(1L);
        usuarioAdmin = new Usuario(1L, "Admin", "admin@example.com", "senha", Cargo.ADMINISTRADOR, revenda);
        usuarioGerente = new Usuario(2L, "Gerente", "gerente@example.com", "senha", Cargo.GERENTE, revenda);
        usuarioProprietario = new Usuario(3L, "Proprietario", "proprietario@example.com", "senha", Cargo.PROPRIETARIO, revenda);
    }

    private void mockSecurityContext(Usuario usuario) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuario);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void loadUserByUsername_DeveRetornarUsuario_QuandoEmailExiste() {
        when(usuarioRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(usuarioAdmin));

        Usuario result = (Usuario) usuarioService.loadUserByUsername("admin@example.com");

        assertEquals(usuarioAdmin, result);
        verify(usuarioRepository).findByEmail("admin@example.com");
    }

    @Test
    void loadUserByUsername_DeveLancarExcecao_QuandoEmailNaoExiste() {
        when(usuarioRepository.findByEmail("naoexiste@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> usuarioService.loadUserByUsername("naoexiste@example.com"));
    }

    @Test
    void findAll_DeveRetornarTodosUsuarios_QuandoUsuarioAdmin() {
        mockSecurityContext(usuarioAdmin);
        Usuario usuario = new Usuario();
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        UsuarioResponseDTO dto = new UsuarioResponseDTO(1L, "Admin", "admin@example.com", Cargo.ADMINISTRADOR, 1L, null);
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(dto);

        List<UsuarioResponseDTO> result = usuarioService.findAll();

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
        verify(usuarioRepository).findAll();
    }

    @Test
    void findAll_DeveRetornarUsuariosDaRevenda_QuandoUsuarioNaoAdmin() {
        mockSecurityContext(usuarioGerente);
        Usuario usuario = new Usuario();
        when(usuarioRepository.findAllByRevenda_Id(1L)).thenReturn(List.of(usuario));
        UsuarioResponseDTO dto = new UsuarioResponseDTO(2L, "Gerente", "gerente@example.com", Cargo.GERENTE, 1L, null);
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(dto);

        List<UsuarioResponseDTO> result = usuarioService.findAll();

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
        verify(usuarioRepository).findAllByRevenda_Id(1L);
    }

    @Test
    void findById_DeveRetornarUsuario_QuandoUsuarioTemAcesso() {
        mockSecurityContext(usuarioGerente);
        Usuario usuario = new Usuario();
        usuario.setRevenda(revenda);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        UsuarioResponseDTO dto = new UsuarioResponseDTO(2L, "Gerente", "gerente@example.com", Cargo.GERENTE, 1L, null);
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(dto);

        UsuarioResponseDTO result = usuarioService.findById(2L);

        assertEquals(dto, result);
        verify(usuarioRepository).findById(2L);
    }

    @Test
    void findById_DeveLancarForbidden_QuandoUsuarioSemAcesso() {
        mockSecurityContext(usuarioGerente);
        Usuario usuario = new Usuario();
        usuario.setRevenda(new Revenda(2L));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> usuarioService.findById(3L));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void findByEmail_DeveRetornarUsuario_QuandoUsuarioTemAcesso() {
        mockSecurityContext(usuarioGerente);
        Usuario usuario = new Usuario();
        usuario.setRevenda(revenda);
        when(usuarioRepository.findByEmail("gerente@example.com")).thenReturn(Optional.of(usuario));
        UsuarioResponseDTO dto = new UsuarioResponseDTO(2L, "Gerente", "gerente@example.com", Cargo.GERENTE, 1L, null);
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(dto);

        UsuarioResponseDTO result = usuarioService.findByEmail("gerente@example.com");

        assertEquals(dto, result);
        verify(usuarioRepository).findByEmail("gerente@example.com");
    }

    @Test
    void findByEmail_DeveLancarForbidden_QuandoUsuarioSemAcesso() {
        mockSecurityContext(usuarioGerente);
        Usuario usuario = new Usuario();
        usuario.setRevenda(new Revenda(2L));
        when(usuarioRepository.findByEmail("outro@example.com")).thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> usuarioService.findByEmail("outro@example.com"));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void save_DeveLancarForbidden_QuandoGerenteForaDaRevenda() {
        mockSecurityContext(usuarioGerente);
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Novo", "novo@example.com", "123", Cargo.ASSISTENTE, 2L);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> usuarioService.save(dto));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void save_DeveLancarBadRequest_QuandoEmailDuplicado() {
        mockSecurityContext(usuarioAdmin);
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Novo", "novo@example.com", "123", Cargo.ASSISTENTE, 1L);
        when(usuarioRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new Usuario()));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> usuarioService.save(dto));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void update_DeveAtualizarUsuario_QuandoProprietarioNaPropriaRevenda() {
        mockSecurityContext(usuarioProprietario);
        UsuarioRequestDTO dto = mock(UsuarioRequestDTO.class);
        when(dto.getNome()).thenReturn("Novo Nome");
        when(dto.getEmail()).thenReturn("novo@example.com");
        when(dto.getSenha()).thenReturn("123");
        when(dto.getCargo()).thenReturn(Cargo.ASSISTENTE);
        when(dto.getRevendaId()).thenReturn(1L);
        assertEquals(1L, dto.getRevendaId(), "revendaId deve ser 1L");
        Usuario usuario = new Usuario();
        usuario.setRevenda(revenda);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(revendaRepository.findById(1L)).thenReturn(Optional.of(revenda));
        when(passwordEncoder.encode("123")).thenReturn("encoded123");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        UsuarioResponseDTO responseDTO = new UsuarioResponseDTO(2L, "Novo Nome", "novo@example.com", Cargo.ASSISTENTE, 1L, null);
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(responseDTO);

        UsuarioResponseDTO result = usuarioService.update(2L, dto);

        assertEquals(responseDTO, result);
        verify(usuarioRepository).save(any(Usuario.class));
        verify(revendaRepository).findById(1L);
    }

    @Test
    void update_DeveLancarForbidden_QuandoProprietarioForaDaRevenda() {
        mockSecurityContext(usuarioProprietario);
        Usuario usuario = new Usuario();
        usuario.setRevenda(new Revenda(2L));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Novo Nome", "novo@example.com", "123", Cargo.ASSISTENTE, 2L);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> usuarioService.update(2L, dto));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void delete_DeveDeletarUsuario_QuandoAdmin() {
        mockSecurityContext(usuarioAdmin);
        Usuario usuario = new Usuario();
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));

        usuarioService.delete(2L);

        verify(usuarioRepository).deleteById(2L);
    }

    @Test
    void delete_DeveLancarForbidden_QuandoGerenteForaDaRevenda() {
        mockSecurityContext(usuarioGerente);
        Usuario usuario = new Usuario();
        usuario.setRevenda(new Revenda(2L));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> usuarioService.delete(2L));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void toUsuario_DeveRetornarUsuario_QuandoIdExiste() {
        UsuarioResponseDTO dto = new UsuarioResponseDTO(1L, "Admin", "admin@example.com", Cargo.ADMINISTRADOR, 1L, null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioAdmin));

        Usuario result = usuarioService.toUsuario(dto);

        assertEquals(usuarioAdmin, result);
        verify(usuarioRepository).findById(1L);
    }

    @Test
    void toUsuario_DeveLancarNotFound_QuandoIdNaoExiste() {
        UsuarioResponseDTO dto = new UsuarioResponseDTO(1L, "Admin", "admin@example.com", Cargo.ADMINISTRADOR, 1L, null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> usuarioService.toUsuario(dto));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}