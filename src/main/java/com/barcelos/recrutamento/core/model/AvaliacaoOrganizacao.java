package com.barcelos.recrutamento.core.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class AvaliacaoOrganizacao {
    private final UUID id;
    private final UUID processoId;
    private final UUID candidatoUsuarioId;
    private final UUID organizacaoId;
    private final int nota;
    private final String comentario;
    private final LocalDateTime criadoEm;
    private final LocalDateTime atualizadoEm;

    private AvaliacaoOrganizacao(UUID id, UUID processoId, UUID candidatoUsuarioId,
                                 UUID organizacaoId, int nota, String comentario,
                                 LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.processoId = Objects.requireNonNull(processoId, "processoId must not be null");
        this.candidatoUsuarioId = Objects.requireNonNull(candidatoUsuarioId, "candidatoUsuarioId must not be null");
        this.organizacaoId = Objects.requireNonNull(organizacaoId, "organizacaoId must not be null");

        if (nota < 1 || nota > 5) {
            throw new IllegalArgumentException("nota must be between 1 and 5");
        }
        this.nota = nota;

        if (comentario == null || comentario.isBlank()) {
            throw new IllegalArgumentException("comentario must not be null or blank");
        }
        this.comentario = comentario;

        this.criadoEm = Objects.requireNonNull(criadoEm, "criadoEm must not be null");
        this.atualizadoEm = Objects.requireNonNull(atualizadoEm, "atualizadoEm must not be null");
    }

    
    public static AvaliacaoOrganizacao nova(UUID processoId, UUID candidatoUsuarioId,
                                           UUID organizacaoId, int nota, String comentario) {
        LocalDateTime agora = LocalDateTime.now();
        return new AvaliacaoOrganizacao(
            UUID.randomUUID(),
            processoId,
            candidatoUsuarioId,
            organizacaoId,
            nota,
            comentario,
            agora,
            agora
        );
    }

    
    public static AvaliacaoOrganizacao rehydrate(UUID id, UUID processoId, UUID candidatoUsuarioId,
                                                UUID organizacaoId, int nota, String comentario,
                                                LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        return new AvaliacaoOrganizacao(id, processoId, candidatoUsuarioId, organizacaoId,
                                       nota, comentario, criadoEm, atualizadoEm);
    }

    
    public AvaliacaoOrganizacao atualizar(int novaNota, String novoComentario) {
        return new AvaliacaoOrganizacao(id, processoId, candidatoUsuarioId, organizacaoId,
                                       novaNota, novoComentario, criadoEm, LocalDateTime.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getProcessoId() {
        return processoId;
    }

    public UUID getCandidatoUsuarioId() {
        return candidatoUsuarioId;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public int getNota() {
        return nota;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    
    public boolean isPositiva() {
        return nota >= 4;
    }

    
    public boolean isNegativa() {
        return nota <= 2;
    }
}
