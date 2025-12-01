package com.barcelos.recrutamento.api.dto;

import java.util.UUID;

public record CadastrarRecrutadorViaConviteResponse(
        UUID usuarioId,
        String nome,
        String email,
        UUID organizacaoId
) {
}

