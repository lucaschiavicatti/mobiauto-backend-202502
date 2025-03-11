package com.mobiauto.backend.controller;

import com.mobiauto.backend.dto.LoginRequestDTO;
import com.mobiauto.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDTO loginDTO) {
        String token = authService.authenticate(loginDTO);
        return ResponseEntity.ok(token);
    }
}