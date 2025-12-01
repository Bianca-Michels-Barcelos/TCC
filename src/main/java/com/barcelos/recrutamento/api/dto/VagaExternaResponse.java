package com.barcelos.recrutamento.api.dto;

import com.barcelos.recrutamento.core.model.VagaExterna;

import java.time.LocalDateTime;
import java.util.UUID;

public record VagaExternaResponse(
        UUID id,
        String titulo,
        String descricao,
        String requisitos,
        String arquivoCurriculo,
        String modeloCurriculo,
        UUID candidatoUsuarioId,
        boolean ativo,
        LocalDateTime criadoEm
) {
    public static VagaExternaResponse fromDomain(VagaExterna vagaExterna) {
        return new VagaExternaResponse(
                vagaExterna.getId(),
                vagaExterna.getTitulo(),
                vagaExterna.getDescricao(),
                vagaExterna.getRequisitos(),
                vagaExterna.getArquivoCurriculo(),
                vagaExterna.getModeloCurriculo() != null ? vagaExterna.getModeloCurriculo().name() : null,
                vagaExterna.getCandidatoUsuarioId(),
                vagaExterna.getAtivo(),
                vagaExterna.getCriadoEm()
        );
    }
}
