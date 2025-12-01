package com.barcelos.recrutamento.api.dto;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UUID usuarioId,
        String nome,
        String email,
        List<String> roles
) {
    public LoginResponse(String accessToken, String refreshToken, UUID usuarioId, String nome, String email, List<String> roles) {
        this(accessToken, refreshToken, "Bearer", usuarioId, nome, email, roles);
    }
}
