package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AtualizarExperienciaRequest(
        @NotBlank String cargo,
        @NotBlank String empresa,
        @NotBlank String descricao,
        @NotNull LocalDate dataInicio,
        LocalDate dataFim
) {
}
