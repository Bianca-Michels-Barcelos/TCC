package com.barcelos.recrutamento.api.dto;

import com.barcelos.recrutamento.core.model.ModalidadeTrabalho;
import com.barcelos.recrutamento.core.model.StatusVaga;
import com.barcelos.recrutamento.core.model.TipoContrato;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CriarVagaRequest(
        @NotNull UUID organizacaoId,
        @NotNull UUID recrutadorUsuarioId,
        @NotBlank String titulo,
        @NotBlank String descricao,
        @NotBlank String requisitos,
        @PositiveOrZero BigDecimal salario,
        @NotNull LocalDate dataPublicacao,
        @NotNull StatusVaga status,
        @NotNull TipoContrato tipoContrato,
        @NotNull ModalidadeTrabalho modalidade,
        @NotBlank String horarioTrabalho,
        UUID nivelExperienciaId,
        String cidade,
        String uf,
        List<UUID> beneficioIds
) {
}
