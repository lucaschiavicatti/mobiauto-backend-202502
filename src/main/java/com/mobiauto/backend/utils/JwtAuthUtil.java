package com.mobiauto.backend.utils;

import com.mobiauto.backend.model.Cargo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

public final class JwtAuthUtil {
    private JwtAuthUtil() {} // Construtor privado para evitar instância

    public static Jwt getJwt() {
        return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static List<Cargo> getCargosFromJwt(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null || roles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nenhum cargo encontrado no token");
        }
        try {
            return roles.stream()
                    .map(Cargo::valueOf)
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cargo inválido no token: " + roles);
        }
    }
}

