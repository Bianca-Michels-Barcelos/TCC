package com.barcelos.recrutamento.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record HistoricoEtapaProcessoResponse(
        UUID id,
        UUID processoSeletivoId,
        UUID etapaProcessoId,
        String etapaProcessoNome,
        UUID usuarioResponsavelId,
        LocalDateTime dataMovimentacao,
        String feedback,
        String acao
) {
}
