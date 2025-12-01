package com.barcelos.recrutamento.data.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class MembroOrganizacaoId implements Serializable {
    private UUID organizacaoId;
    private UUID usuarioId;

    public MembroOrganizacaoId() {
    }

    public MembroOrganizacaoId(UUID organizacaoId, UUID usuarioId) {
        this.organizacaoId = organizacaoId;
        this.usuarioId = usuarioId;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MembroOrganizacaoId that)) return false;
        return Objects.equals(organizacaoId, that.organizacaoId) && Objects.equals(usuarioId, that.usuarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizacaoId, usuarioId);
    }
}