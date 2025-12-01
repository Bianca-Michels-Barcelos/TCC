package com.barcelos.recrutamento.core.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class HistoricoEtapaProcesso {
    private final UUID id;
    private final UUID processoId;
    private final UUID etapaAnteriorId;
    private final UUID etapaNovaId;
    private final UUID usuarioId;
    private final String feedback;
    private final LocalDateTime dataMudanca;

    private HistoricoEtapaProcesso(UUID id, UUID processoId, UUID etapaAnteriorId,
                                   UUID etapaNovaId, UUID usuarioId, String feedback,
                                   LocalDateTime dataMudanca) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.processoId = Objects.requireNonNull(processoId, "processoId must not be null");
        this.etapaAnteriorId = etapaAnteriorId;
        this.etapaNovaId = Objects.requireNonNull(etapaNovaId, "etapaNovaId must not be null");
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId must not be null");
        if (feedback == null || feedback.isBlank()) {
            throw new IllegalArgumentException("feedback must not be null or blank");
        }
        this.feedback = feedback;
        this.dataMudanca = Objects.requireNonNull(dataMudanca, "dataMudanca must not be null");
    }

    
    public static HistoricoEtapaProcesso novo(UUID processoId, UUID etapaAnteriorId,
                                              UUID etapaNovaId, UUID usuarioId, String feedback) {
        return new HistoricoEtapaProcesso(
            UUID.randomUUID(),
            processoId,
            etapaAnteriorId,
            etapaNovaId,
            usuarioId,
            feedback,
            LocalDateTime.now()
        );
    }

    
    public static HistoricoEtapaProcesso rehydrate(UUID id, UUID processoId, UUID etapaAnteriorId,
                                                   UUID etapaNovaId, UUID usuarioId, String feedback,
                                                   LocalDateTime dataMudanca) {
        return new HistoricoEtapaProcesso(id, processoId, etapaAnteriorId, etapaNovaId,
                                         usuarioId, feedback, dataMudanca);
    }

    public UUID getId() {
        return id;
    }

    public UUID getProcessoId() {
        return processoId;
    }

    public UUID getEtapaAnteriorId() {
        return etapaAnteriorId;
    }

    public UUID getEtapaNovaId() {
        return etapaNovaId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getFeedback() {
        return feedback;
    }

    public LocalDateTime getDataMudanca() {
        return dataMudanca;
    }

    
    public boolean isPrimeiraMudanca() {
        return etapaAnteriorId == null;
    }
}
