package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AtualizarCertificadoRequest(
        @NotBlank String titulo,
        @NotBlank String instituicao,
        @NotNull LocalDate dataEmissao,
        LocalDate dataValidade,
        String descricao
) {
}
