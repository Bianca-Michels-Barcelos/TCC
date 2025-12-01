package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AtualizarHistoricoRequest(
        @NotBlank String titulo,
        @NotBlank String instituicao,
        String descricao,
        @NotNull LocalDate dataInicio,
        LocalDate dataFim
) {
}
