package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DesistirCandidaturaRequest(
        @NotBlank String motivo
) {
}
