package com.barcelos.recrutamento.api.dto;

import com.barcelos.recrutamento.data.entity.PapelOrganizacao;
import jakarta.validation.constraints.NotNull;

public record AlterarPapelRequest(
        @NotNull PapelOrganizacao papel
) {
}
