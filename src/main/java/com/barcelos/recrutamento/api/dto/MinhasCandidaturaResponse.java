package com.barcelos.recrutamento.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MinhasCandidaturaResponse(
        UUID id,
        UUID vagaId,
        VagaResumo vaga,
        String status,
        LocalDate dataCandidatura,
        String etapaAtual,
        BigDecimal compatibilidade
) {
    public record VagaResumo(
            String titulo,
            String descricao,
            String modalidade,
            BigDecimal salario,
            OrganizacaoResumo organizacao,
            EnderecoResumo endereco
    ) {}

    public record OrganizacaoResumo(
            String nome
    ) {}

    public record EnderecoResumo(
            String cidade,
            String uf
    ) {}
}
