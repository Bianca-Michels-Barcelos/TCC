package com.barcelos.recrutamento.api.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BuscarCandidatoResponse(
        UUID usuarioId,
        String nome,
        String email,
        String cidade,
        String uf,
        LocalDate dataNascimento,
        List<String> competencias,
        List<String> experiencias,
        int scoreRelevancia,
        String resumo,
        boolean jaConvidado
) {
}
