package com.barcelos.recrutamento.api.dto.dashboard;

import java.util.UUID;

public record VagaAtencaoResponse(
    UUID vagaId,
    String titulo,
    String motivoAlerta,
    Integer quantidadePendente,
    Integer diasSemAtualizacao
) {
}
