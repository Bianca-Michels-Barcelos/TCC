package com.barcelos.recrutamento.api.dto;

import java.util.UUID;

public record PortfolioResponse(
        UUID id,
        String titulo,
        String link
) {
}
