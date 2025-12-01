package com.barcelos.recrutamento.core.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class VagaSalva {
    private final UUID id;
    private final UUID vagaId;
    private final UUID usuarioId;
    private final LocalDateTime salvaEm;

    private VagaSalva(UUID id, UUID vagaId, UUID usuarioId, LocalDateTime salvaEm) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.vagaId = Objects.requireNonNull(vagaId, "vagaId must not be null");
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId must not be null");
        this.salvaEm = Objects.requireNonNull(salvaEm, "salvaEm must not be null");
    }

    
    public static VagaSalva nova(UUID vagaId, UUID usuarioId) {
        return new VagaSalva(
            UUID.randomUUID(),
            vagaId,
            usuarioId,
            LocalDateTime.now()
        );
    }

    
    public static VagaSalva rehydrate(UUID id, UUID vagaId, UUID usuarioId, LocalDateTime salvaEm) {
        return new VagaSalva(id, vagaId, usuarioId, salvaEm);
    }

    public UUID getId() {
        return id;
    }

    public UUID getVagaId() {
        return vagaId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public LocalDateTime getSalvaEm() {
        return salvaEm;
    }
}
