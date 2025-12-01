package com.barcelos.recrutamento.api.dto;

import com.barcelos.recrutamento.core.model.StatusCandidatura;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusCandidaturaRequest(@NotNull StatusCandidatura novoStatus) {
}