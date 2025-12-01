package com.barcelos.recrutamento.api.dto;

import com.barcelos.recrutamento.data.entity.NivelCompetencia;

import java.util.UUID;

public record CompetenciaResponse(
        UUID id,
        String titulo,
        String descricao,
        NivelCompetencia nivel
) {
}
