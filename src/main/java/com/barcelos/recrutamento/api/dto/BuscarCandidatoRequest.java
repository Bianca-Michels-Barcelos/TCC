package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BuscarCandidatoRequest(
        @NotNull(message = "ID da vaga é obrigatório")
        UUID vagaId,
        String consulta
) {
}
