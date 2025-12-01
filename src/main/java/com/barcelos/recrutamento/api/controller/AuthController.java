package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.LoginRequest;
import com.barcelos.recrutamento.api.dto.LoginResponse;
import com.barcelos.recrutamento.api.dto.RefreshTokenRequest;
import com.barcelos.recrutamento.core.service.AuthService;
import jakarta.validation.Valid;
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
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = authService.login(request.email(), request.senha());
        var response = new LoginResponse(
                result.accessToken(),
                result.refreshToken(),
                result.usuarioId(),
                result.nome(),
                result.email(),
                result.roles()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var result = authService.refresh(request.refreshToken());
        var response = new LoginResponse(
                result.accessToken(),
                result.refreshToken(),
                null,
                null,
                null,
                null
        );
        return ResponseEntity.ok(response);
    }
}
