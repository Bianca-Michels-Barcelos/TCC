package com.barcelos.recrutamento.core.model;

import java.time.Instant;
import java.util.UUID;

public final class RefreshToken {
    private final UUID id;
    private final String token;
    private final UUID usuarioId;
    private final Instant expiraEm;
    private final Instant criadoEm;
    private final boolean revogado;

    private RefreshToken(UUID id, String token, UUID usuarioId, Instant expiraEm, Instant criadoEm, boolean revogado) {
        this.id = id;
        this.token = token;
        this.usuarioId = usuarioId;
        this.expiraEm = expiraEm;
        this.criadoEm = criadoEm;
        this.revogado = revogado;
    }

    public static RefreshToken novo(String token, UUID usuarioId, Instant expiraEm) {
        return new RefreshToken(null, token, usuarioId, expiraEm, Instant.now(), false);
    }

    public static RefreshToken reconstituir(UUID id, String token, UUID usuarioId, Instant expiraEm, Instant criadoEm, boolean revogado) {
        return new RefreshToken(id, token, usuarioId, expiraEm, criadoEm, revogado);
    }

    public RefreshToken revogar() {
        return new RefreshToken(id, token, usuarioId, expiraEm, criadoEm, true);
    }

    public boolean isExpirado() {
        return Instant.now().isAfter(expiraEm);
    }

    public boolean isValido() {
        return !revogado && !isExpirado();
    }

    public UUID getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public Instant getExpiraEm() {
        return expiraEm;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public boolean isRevogado() {
        return revogado;
    }
}
