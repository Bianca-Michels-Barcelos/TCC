package com.barcelos.recrutamento.api.dto;

import com.barcelos.recrutamento.core.model.TipoEtapa;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AdicionarEtapaVagaRequest(
        @NotBlank String nome,
        String descricao,
        @NotNull TipoEtapa tipo,
        @Min(1) int ordem,
        LocalDateTime dataInicio,
        LocalDateTime dataFim
) {
}
