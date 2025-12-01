package com.barcelos.recrutamento.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConviteRecrutadorResponse(
        UUID id,
        UUID organizacaoId,
        String email,
        String status,
        LocalDateTime dataEnvio,
        LocalDateTime dataExpiracao,
        LocalDateTime dataAceite
) {
}
