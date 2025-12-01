package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdicionarNivelExperienciaRequest(
        @NotBlank @Size(max = 50) String descricao
) {
}