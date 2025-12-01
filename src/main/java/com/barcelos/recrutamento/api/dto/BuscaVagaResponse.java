package com.barcelos.recrutamento.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BuscaVagaResponse(
        UUID vagaId,
        String titulo,
        String descricao,
        String requisitos,
        String nomeOrganizacao,
        BigDecimal salario,
        String modalidade,
        String cidade,
        String uf,
        int scoreRelevancia,
        Integer percentualCompatibilidade,
        String justificativa,
        Boolean usouIA
) {
}
