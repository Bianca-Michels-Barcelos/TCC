package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.Size;

public record AtualizarVagaExternaRequest(
        @Size(max = 50, message = "Título deve ter no máximo 50 caracteres")
        String titulo,

        String descricao,

        String requisitos
) {
}
