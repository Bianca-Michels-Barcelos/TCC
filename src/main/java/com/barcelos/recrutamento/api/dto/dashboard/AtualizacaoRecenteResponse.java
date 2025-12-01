package com.barcelos.recrutamento.api.dto.dashboard;

import java.time.LocalDateTime;
import java.util.UUID;

public record AtualizacaoRecenteResponse(
    String tipo,
    UUID candidaturaId,
    UUID vagaId,
    String tituloVaga,
    String nomeOrganizacao,
    String descricao,
    LocalDateTime dataHora
) {
}
