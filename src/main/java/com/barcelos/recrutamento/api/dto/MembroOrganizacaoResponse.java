package com.barcelos.recrutamento.api.dto;

import com.barcelos.recrutamento.data.entity.PapelOrganizacao;

import java.util.UUID;

public record MembroOrganizacaoResponse(
        UUID usuarioId,
        PapelOrganizacao papel,
        boolean ativo
) {
}
