package com.barcelos.recrutamento.api.dto;

import com.barcelos.recrutamento.core.model.ProcessoSeletivo;

import java.util.UUID;

public record ProcessoSeletivoComVagaId(
        ProcessoSeletivo processo,
        UUID vagaId
) {
}
