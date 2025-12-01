package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EnviarConviteRequest(
        @NotNull(message = "ID da vaga é obrigatório")
        UUID vagaId,

        @NotNull(message = "ID do candidato é obrigatório")
        UUID candidatoUsuarioId,

        String mensagem
) {
}
