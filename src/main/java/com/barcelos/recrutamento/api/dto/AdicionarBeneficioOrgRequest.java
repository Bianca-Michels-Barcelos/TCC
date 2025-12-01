package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AdicionarBeneficioOrgRequest(
        @NotBlank String nome,
        @NotBlank String descricao
) {
}