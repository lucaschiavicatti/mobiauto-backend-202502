package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.LoginRequestDTO;
import com.mobiauto.backend.dto.LoginResponseDTO;
import com.mobiauto.backend.model.Usuario;
import com.mobiauto.backend.repository.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static java.time.Instant.now;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private static final long EXPIRES_IN = 300L;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    public LoginResponseDTO authenticate(LoginRequestDTO loginDTO) {
        Usuario usuario = usuarioRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));
        if (!usuario.isLoginCorrect(passwordEncoder, loginDTO)) {
            throw new BadCredentialsException("Credenciais inválidas");
        }
        var jwtValue = jwtValue(usuario);
        return new LoginResponseDTO(jwtValue, EXPIRES_IN);
    }

    private String jwtValue(Usuario usuario) {
        var claims = JwtClaimsSet.builder()
                .issuer("mobiauto")
                .subject(usuario.getEmail())
                .issuedAt(now())
                .expiresAt(now().plusSeconds(EXPIRES_IN))
                .claim("roles", Collections.singletonList(usuario.getCargo().name()))
                .claim("revendaId", usuario.getRevenda().getId().toString())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}