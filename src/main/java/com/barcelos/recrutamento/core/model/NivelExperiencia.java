package com.barcelos.recrutamento.core.model;

import java.util.Objects;
import java.util.UUID;

public final class NivelExperiencia {
    private final UUID id;
    private final UUID organizacaoId;
    private final String descricao;
    private final boolean ativo;

    private NivelExperiencia(UUID id, UUID organizacaoId, String descricao, boolean ativo) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.organizacaoId = Objects.requireNonNull(organizacaoId, "organizacaoId must not be null");
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("descricao must not be blank");
        }
        if (descricao.length() > 50) {
            throw new IllegalArgumentException("descricao must be at most 50 characters");
        }
        this.descricao = descricao;
        this.ativo = ativo;
    }

    
    public static NivelExperiencia novo(UUID organizacaoId, String descricao) {
        return new NivelExperiencia(UUID.randomUUID(), organizacaoId, descricao, true);
    }

    public static NivelExperiencia atualizar(UUID id, UUID organizacaoId, String descricao) {
        return new NivelExperiencia(id, organizacaoId, descricao, true);
    }

    
    public static NivelExperiencia rehydrate(UUID id, UUID organizacaoId, String descricao, boolean ativo) {
        return new NivelExperiencia(id, organizacaoId, descricao, ativo);
    }

    
    public NivelExperiencia comDescricao(String novaDescricao) {
        return new NivelExperiencia(id, organizacaoId, novaDescricao, ativo);
    }

    
    public NivelExperiencia ativar() {
        if (ativo) {
            return this;
        }
        return new NivelExperiencia(id, organizacaoId, descricao, true);
    }

    
    public NivelExperiencia desativar() {
        if (!ativo) {
            return this;
        }
        return new NivelExperiencia(id, organizacaoId, descricao, false);
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
