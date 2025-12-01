package com.barcelos.recrutamento.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record ProcessoSeletivoComCandidato(
        UUID processoId,
        UUID candidaturaId,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        LocalDateTime dataUltimaMudanca,

        UUID etapaAtualId,
        String etapaAtualNome,
        String etapaAtualDescricao,
        Integer etapaAtualOrdem,
        String etapaAtualStatus,

        UUID candidatoUsuarioId,
        String candidatoNome,
        String candidatoEmail,

        String statusCandidatura,
        LocalDateTime dataCandidatura,
        BigDecimal compatibilidade,
        String arquivoCurriculo,

        UUID vagaId,
        String vagaTitulo
) {
    public boolean isFinalizado() {
        return dataFim != null;
    }
}
