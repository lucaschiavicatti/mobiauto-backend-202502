package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.LoginRequestDTO;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.Usuario;
import com.mobiauto.backend.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AuthService authService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        ReflectionTestUtils.setField(authService, "secretKeyString", "mobiauto-secret-key-1234567890-abcdefghijklmnopqrstuvwxyz1234567890-abc");
        ReflectionTestUtils.setField(authService, "EXPIRATION_TIME", 86400000L);
        authService.init();
    }

    @Test
    void loadUserByUsername_DeveRetornarUsuario_QuandoEmailExiste() {
        String email = "joao@example.com";
        Usuario usuario = new Usuario(1L, "João", email, passwordEncoder.encode("senha123"), Cargo.ADMINISTRADOR, new Revenda(1L));
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        UserDetails userDetails = authService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("ROLE_ADMINISTRADOR", userDetails.getAuthorities().iterator().next().getAuthority());
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    @Test
    void loadUserByUsername_DeveLancarExcecao_QuandoEmailNaoExiste() {
        String email = "invalido@example.com";
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.loadUserByUsername(email);
        });
        assertEquals("Usuário não encontrado: " + email, exception.getMessage());
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    @Test
    void authenticate_DeveGerarToken_QuandoCredenciaisValidas() {
        String email = "joao@example.com";
        String senha = "senha123";
        String senhaCriptografada = passwordEncoder.encode(senha);
        Usuario usuario = new Usuario(1L, "João", email, senhaCriptografada, Cargo.ADMINISTRADOR, new Revenda(1L));
        LoginRequestDTO loginDTO = new LoginRequestDTO(email, senha);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        String token = authService.authenticate(loginDTO);

        assertNotNull(token);
        String subject = Jwts.parser()
                .setSigningKey("mobiauto-secret-key-1234567890-abcdefghijklmnopqrstuvwxyz1234567890-abc".getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        assertEquals(email, subject);
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    @Test
    void authenticate_DeveLancarExcecao_QuandoEmailNaoExiste() {
        String email = "invalido@example.com";
        LoginRequestDTO loginDTO = new LoginRequestDTO(email, "senha123");
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticate(loginDTO);
        });
        assertEquals("Credenciais inválidas", exception.getMessage());
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    @Test
    void authenticate_DeveLancarExcecao_QuandoSenhaIncorreta() {
        String email = "joao@example.com";
        String senhaCorreta = "senha123";
        String senhaIncorreta = "senhaErrada";
        String senhaCriptografada = passwordEncoder.encode(senhaCorreta);
        Usuario usuario = new Usuario(1L, "João", email, senhaCriptografada, Cargo.ADMINISTRADOR, new Revenda(1L));
        LoginRequestDTO loginDTO = new LoginRequestDTO(email, senhaIncorreta);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticate(loginDTO);
        });
        assertEquals("Credenciais inválidas", exception.getMessage());
        verify(usuarioRepository, times(1)).findByEmail(email);
    }
}