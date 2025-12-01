package com.barcelos.recrutamento.api.dto;

import com.barcelos.recrutamento.core.model.ModeloCurriculoEnum;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GerarCurriculoAIRequest(
        @NotNull(message = "ID da vaga é obrigatório")
        UUID vagaId,

        ModeloCurriculoEnum modelo,

        String observacoes
) {
    public GerarCurriculoAIRequest {
        if (modelo == null) {
            modelo = ModeloCurriculoEnum.PROFISSIONAL;
        }
    }
}
