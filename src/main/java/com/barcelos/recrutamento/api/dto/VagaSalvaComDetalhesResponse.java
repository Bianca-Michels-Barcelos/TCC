package com.barcelos.recrutamento.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record VagaSalvaComDetalhesResponse(
        UUID id,
        UUID vagaId,
        LocalDateTime salvaEm,
        VagaInfo vaga,
        OrganizacaoInfo organizacao
) {
    public record VagaInfo(
            UUID id,
            String titulo,
            String descricao,
            String requisitos,
            BigDecimal salario,
            String modalidade,
            String tipoContrato,
            String status,
            LocalDate dataPublicacao,
            EnderecoInfo endereco
    ) {}

    public record EnderecoInfo(
            String cidade,
            String uf
    ) {}

    public record OrganizacaoInfo(
            UUID id,
            String nome
    ) {}
}
