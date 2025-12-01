package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AdicionarProjetoExperienciaRequest(
        @NotBlank String nome,
        @NotBlank String descricao
) {
}