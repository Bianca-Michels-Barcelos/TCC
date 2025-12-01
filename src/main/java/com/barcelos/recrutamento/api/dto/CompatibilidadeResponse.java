package com.barcelos.recrutamento.api.dto;

import java.util.UUID;

public record CompatibilidadeResponse(
        UUID candidatoUsuarioId,
        UUID vagaId,
        int percentualCompatibilidade,
        String justificativa,
        boolean usouIA
) {
}
