package com.barcelos.recrutamento.core.model;

import com.barcelos.recrutamento.data.entity.PapelOrganizacao;

import java.util.Objects;
import java.util.UUID;

public final class MembroOrganizacao {
    private final UUID organizacaoId;
    private final UUID usuarioId;
    private final PapelOrganizacao papel;
    private final boolean ativo;

    private MembroOrganizacao(UUID organizacaoId, UUID usuarioId, PapelOrganizacao papel, boolean ativo) {
        this.organizacaoId = Objects.requireNonNull(organizacaoId, "organizacaoId must not be null");
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId must not be null");
        this.papel = Objects.requireNonNull(papel, "papel must not be null");
        this.ativo = ativo;
    }

    
    public static MembroOrganizacao novo(UUID organizacaoId, UUID usuarioId, PapelOrganizacao papel) {
        return new MembroOrganizacao(organizacaoId, usuarioId, papel, true);
    }

    
    public static MembroOrganizacao rehydrate(UUID organizacaoId, UUID usuarioId, PapelOrganizacao papel, boolean ativo) {
        return new MembroOrganizacao(organizacaoId, usuarioId, papel, ativo);
    }

    
    public MembroOrganizacao comPapel(PapelOrganizacao novoPapel) {
        return new MembroOrganizacao(organizacaoId, usuarioId, novoPapel, ativo);
    }

    
    public MembroOrganizacao ativar() {
        if (ativo) return this;
        return new MembroOrganizacao(organizacaoId, usuarioId, papel, true);
    }

    
    public MembroOrganizacao desativar() {
        if (!ativo) return this;
        return new MembroOrganizacao(organizacaoId, usuarioId, papel, false);
    }

    
    public boolean isAdmin() {
        return papel == PapelOrganizacao.ADMIN;
    }

    
    public boolean isRecrutador() {
        return papel == PapelOrganizacao.RECRUTADOR;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public PapelOrganizacao getPapel() {
        return papel;
    }

    public boolean isAtivo() {
        return ativo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MembroOrganizacao that = (MembroOrganizacao) o;
        return Objects.equals(organizacaoId, that.organizacaoId) &&
                Objects.equals(usuarioId, that.usuarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizacaoId, usuarioId);
    }
}
