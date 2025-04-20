package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.LoginRequestDTO;
import com.mobiauto.backend.dto.LoginResponseDTO;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.Usuario;
import com.mobiauto.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private LoginRequestDTO loginRequestDTO;
    private static final String EMAIL = "test@example.com";
    private static final String SENHA = "password123";
    private static final String SENHA_ENCODED = "$2a$10$encodedPassword";
    private static final Long REVENDA_ID = 1L;
    private static final String JWT_TOKEN = "mocked-jwt-token";
    private static final Long EXPIRES_IN = 300L;

    @BeforeEach
    void setUp() {
        Revenda revenda = new Revenda();
        revenda.setId(REVENDA_ID);

        usuario = new Usuario();
        usuario.setEmail(EMAIL);
        usuario.setSenha(SENHA_ENCODED);
        usuario.setCargo(Cargo.ADMINISTRADOR);
        usuario.setRevenda(revenda);

        loginRequestDTO = new LoginRequestDTO(EMAIL, SENHA);
    }

    @Test
    void autenticar_Sucesso_RetornaLoginResponseDTOComClaimsCorretos() {

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(SENHA, SENHA_ENCODED)).thenReturn(true);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn(JWT_TOKEN);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        LoginResponseDTO response = authService.authenticate(loginRequestDTO);

        assertNotNull(response);
        assertEquals(JWT_TOKEN, response.accessToken());
        assertEquals(EXPIRES_IN, response.expiresIn());
        verify(usuarioRepository).findByEmail(EMAIL);
        verify(passwordEncoder).matches(SENHA, SENHA_ENCODED);
        verify(jwtEncoder).encode(argThat(params -> {
            JwtClaimsSet claims = params.getClaims();
            return claims.getClaim("iss").equals("mobiauto") &&
                    claims.getSubject().equals(EMAIL) &&
                    claims.getClaimAsStringList("roles").equals(Collections.singletonList(Cargo.ADMINISTRADOR.name())) &&
                    claims.getClaimAsString("revendaId").equals(REVENDA_ID.toString());
        }));
    }

    @Test
    void autenticar_UsuarioNaoEncontrado_LancaBadCredentialsException() {

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> authService.authenticate(loginRequestDTO));
        assertEquals("Credenciais inválidas", exception.getMessage());
        verify(usuarioRepository).findByEmail(EMAIL);
        verifyNoInteractions(passwordEncoder, jwtEncoder);
    }

    @Test
    void autenticar_SenhaInvalida_LancaBadCredentialsException() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(SENHA, SENHA_ENCODED)).thenReturn(false);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> authService.authenticate(loginRequestDTO));
        assertEquals("Credenciais inválidas", exception.getMessage());
        verify(usuarioRepository).findByEmail(EMAIL);
        verify(passwordEncoder).matches(SENHA, SENHA_ENCODED);
        verifyNoInteractions(jwtEncoder);
    }
}