package com.barcelos.recrutamento.core.event;

import java.util.UUID;

public class PerfilCandidatoAtualizadoEvent {
    private final UUID candidatoUsuarioId;

    public PerfilCandidatoAtualizadoEvent(UUID candidatoUsuarioId) {
        this.candidatoUsuarioId = candidatoUsuarioId;
    }

    public UUID getCandidatoUsuarioId() {
        return candidatoUsuarioId;
    }
}
