package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdicionarPortfolioRequest(
        @NotBlank @Size(max = 100) String titulo,
        @NotBlank @Size(max = 255) String link
) {
}
