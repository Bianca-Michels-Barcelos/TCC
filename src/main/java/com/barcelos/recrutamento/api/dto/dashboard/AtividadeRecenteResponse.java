package com.barcelos.recrutamento.api.dto.dashboard;

import java.time.LocalDateTime;
import java.util.UUID;

public record AtividadeRecenteResponse(
    String tipo,
    UUID candidaturaId,
    UUID processoId,
    UUID vagaId,
    String tituloVaga,
    String nomeCandidato,
    String descricao,
    LocalDateTime dataHora
) {
}
