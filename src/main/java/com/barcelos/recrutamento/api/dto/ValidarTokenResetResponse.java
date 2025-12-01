package com.barcelos.recrutamento.api.dto;

public record ValidarTokenResetResponse(
        boolean valido,
        String email
) {
}

