package com.barcelos.recrutamento.api.dto;

import java.util.UUID;

public record OrganizacaoPublicaResponse(
        UUID id,
        String nome,
        EnderecoInfo endereco
) {
    public record EnderecoInfo(
            String cidade,
            String uf
    ) {
    }
}

