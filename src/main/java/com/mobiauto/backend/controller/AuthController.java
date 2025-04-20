package com.mobiauto.backend.controller;

import com.mobiauto.backend.dto.LoginRequestDTO;
import com.mobiauto.backend.dto.LoginResponseDTO;
import com.mobiauto.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginDTO) {
        return ResponseEntity.ok(authService.authenticate(loginDTO));
    }
}