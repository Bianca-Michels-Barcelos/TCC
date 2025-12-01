package com.barcelos.recrutamento.api.dto;

import java.util.UUID;

public record CandidatoCompatibilidadeResponse(
        UUID candidatoUsuarioId,
        String nome,
        String email,
        int percentualCompatibilidade,
        String justificativa
) {
}
