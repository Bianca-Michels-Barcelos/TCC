package com.barcelos.recrutamento.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ConviteProcessoResponse(
        UUID id,
        UUID vagaId,
        String tituloVaga,
        String descricaoVaga,
        String nomeOrganizacao,
        String modalidade,
        String cidade,
        String uf,
        BigDecimal salarioMinimo,
        BigDecimal salarioMaximo,
        String nomeRecrutador,
        String mensagem,
        String status,
        LocalDateTime dataEnvio,
        LocalDateTime dataExpiracao,
        LocalDateTime dataResposta
) {
}
