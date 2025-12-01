package com.barcelos.recrutamento.api.dto.dashboard;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProximaEtapaResponse(
    UUID processoId,
    UUID candidaturaId,
    UUID vagaId,
    String tituloVaga,
    String nomeOrganizacao,
    String etapaAtual,
    String tipoEtapa,
    String acao,
    LocalDateTime prazo
) {
}
