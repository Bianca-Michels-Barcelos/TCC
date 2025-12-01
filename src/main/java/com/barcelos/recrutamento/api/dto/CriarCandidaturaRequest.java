package com.barcelos.recrutamento.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CriarCandidaturaRequest(
        @NotNull UUID candidatoUsuarioId,
        String arquivoCurriculo,
        String modeloCurriculo,
        String conteudoPersonalizado
) {
}