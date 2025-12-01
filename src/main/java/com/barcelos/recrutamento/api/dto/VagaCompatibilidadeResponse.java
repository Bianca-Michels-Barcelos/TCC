package com.barcelos.recrutamento.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record VagaCompatibilidadeResponse(
        UUID vagaId,
        String titulo,
        String descricao,
        String nomeOrganizacao,
        BigDecimal salario,
        int percentualCompatibilidade,
        String justificativa
) {
}
