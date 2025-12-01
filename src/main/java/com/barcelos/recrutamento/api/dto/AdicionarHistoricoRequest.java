package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AdicionarHistoricoRequest(
        @NotBlank String titulo,
        String descricao,
        @NotBlank String instituicao,
        @NotNull LocalDate dataInicio,
        LocalDate dataFim
) {
}