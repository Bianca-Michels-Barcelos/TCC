package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransferirVagasRequest(
        @NotNull UUID usuarioIdDestino
) {
}
