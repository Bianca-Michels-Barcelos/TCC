package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AvancarEtapaRequest(
        @NotNull UUID etapaId,
        @NotBlank String feedback
) {
}
