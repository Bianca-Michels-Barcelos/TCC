package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtualizarAvaliacaoOrganizacaoRequest(
        @NotNull @Min(1) @Max(5) Integer nota,
        @NotBlank String comentario
) {
}
