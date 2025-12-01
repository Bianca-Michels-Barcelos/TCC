package com.barcelos.recrutamento.api.dto;

import com.barcelos.recrutamento.core.model.vo.EnderecoSimples;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record VagaPublicaResponse(
        UUID id,
        String titulo,
        String descricao,
        String requisitos,
        BigDecimal salario,
        String modalidade,
        String tipoContrato,
        String horarioTrabalho,
        String status,
        LocalDate dataPublicacao,
        EnderecoSimples endereco,
        OrganizacaoInfo organizacao,
        List<BeneficioInfo> beneficios,
        NivelExperienciaInfo nivelExperiencia
) {
    public record OrganizacaoInfo(
            UUID id,
            String nome,
            String descricao
    ) {}

    public record BeneficioInfo(
            UUID id,
            String nome,
            String descricao
    ) {}

    public record NivelExperienciaInfo(
            UUID id,
            String descricao
    ) {}
}
