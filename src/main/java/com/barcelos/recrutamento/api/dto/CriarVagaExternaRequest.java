package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CriarVagaExternaRequest(
        @NotBlank(message = "Título é obrigatório")
        @Size(max = 50, message = "Título deve ter no máximo 50 caracteres")
        String titulo,

        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        @NotBlank(message = "Requisitos são obrigatórios")
        String requisitos,

        @NotNull(message = "ID do candidato é obrigatório")
        UUID candidatoUsuarioId
) {
}
