package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.Positive;

public record BuscaVagaRequest(
        String consulta,

        @Positive(message = "Limite deve ser positivo")
        Integer limite
) {
    public BuscaVagaRequest {

        if (limite == null) {
            limite = 10;
        }
    }
}
