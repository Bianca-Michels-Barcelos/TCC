package com.barcelos.recrutamento.api.dto;

import java.util.List;

public record BuscarCandidatoPageResponse(
        List<BuscarCandidatoResponse> content,
        int currentPage,
        int totalPages,
        long totalElements,
        int size
) {
}

