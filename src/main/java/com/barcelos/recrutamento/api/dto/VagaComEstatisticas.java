package com.barcelos.recrutamento.api.dto;

import com.barcelos.recrutamento.core.model.vo.EnderecoSimples;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record VagaComEstatisticas(
        UUID id,
        UUID organizacaoId,
        UUID recrutadorUsuarioId,
        String titulo,
        String descricao,
        String requisitos,
        BigDecimal salario,
        LocalDate dataPublicacao,
        String status,
        String tipoContrato,
        String modalidade,
        String horarioTrabalho,
        EnderecoSimples endereco,
        boolean ativo,
        String motivoCancelamento,

        UUID nivelExperienciaId,
        String nomeNivelExperiencia,

        Integer totalCandidatos,
        Integer candidatosAtivos,
        Integer candidatosAceitos,
        Integer candidatosRejeitados,
        Integer totalEtapas,
        Integer totalBeneficios,

        LocalDate ultimaAtualizacao
) {
}
