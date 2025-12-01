package com.barcelos.recrutamento.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CertificadoResponse(
        UUID id,
        String titulo,
        String instituicao,
        LocalDate dataEmissao,
        LocalDate dataValidade,
        String descricao
) {
}
