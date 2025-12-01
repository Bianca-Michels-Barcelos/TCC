package com.barcelos.recrutamento.api.dto.dashboard;

import java.time.LocalDateTime;
import java.util.UUID;

public record EntrevistaProximaResponse(
    UUID conviteId,
    UUID vagaId,
    String tituloVaga,
    UUID candidatoId,
    String nomeCandidato,
    String tipoEntrevista,
    LocalDateTime dataEnvio,
    LocalDateTime dataExpiracao,
    String status
) {
}
