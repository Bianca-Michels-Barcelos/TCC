package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelarVagaRequest(
        @NotBlank(message = "Motivo do cancelamento é obrigatório") String motivo
) {
}
