package com.barcelos.recrutamento.api.dto;

import com.barcelos.recrutamento.data.entity.NivelCompetencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdicionarCompetenciaRequest(
        @NotBlank String titulo,
        @NotBlank String descricao,
        @NotNull NivelCompetencia nivel
) {
}
