package com.barcelos.recrutamento.core.model;

import com.barcelos.recrutamento.data.entity.NivelCompetencia;

import java.util.Objects;
import java.util.UUID;

public final class Competencia {
    private final UUID id;
    private final UUID perfilCandidatoId;
    private final String titulo;
    private final String descricao;
    private final NivelCompetencia nivel;
    private final boolean ativo;

    private Competencia(UUID id, UUID perfilCandidatoId, String titulo, String descricao, NivelCompetencia nivel, boolean ativo) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.perfilCandidatoId = Objects.requireNonNull(perfilCandidatoId, "perfilCandidatoId must not be null");
        if (titulo == null || titulo.isBlank()) throw new IllegalArgumentException("titulo must not be blank");
        if (titulo.length() > 80) throw new IllegalArgumentException("titulo max 80 chars");
        this.titulo = titulo;
        if (descricao == null || descricao.isBlank()) throw new IllegalArgumentException("descricao must not be blank");
        this.descricao = descricao;
        this.nivel = Objects.requireNonNull(nivel, "nivel must not be null");
        this.ativo = ativo;
    }

    
    public static Competencia nova(UUID perfilCandidatoId, String titulo, String descricao, NivelCompetencia nivel) {
        return new Competencia(UUID.randomUUID(), perfilCandidatoId, titulo, descricao, nivel, true);
    }

    
    public static Competencia rehydrate(UUID id, UUID perfilCandidatoId, String titulo, String descricao, NivelCompetencia nivel, boolean ativo) {
        return new Competencia(id, perfilCandidatoId, titulo, descricao, nivel, ativo);
    }

    
    public Competencia comTitulo(String novoTitulo) {
        return new Competencia(id, perfilCandidatoId, novoTitulo, descricao, nivel, ativo);
    }

    
    public Competencia comDescricao(String novaDescricao) {
        return new Competencia(id, perfilCandidatoId, titulo, novaDescricao, nivel, ativo);
    }

    
    public Competencia comNivel(NivelCompetencia novoNivel) {
        return new Competencia(id, perfilCandidatoId, titulo, descricao, novoNivel, ativo);
    }

    
    public Competencia ativar() {
        if (ativo) return this;
        return new Competencia(id, perfilCandidatoId, titulo, descricao, nivel, true);
    }

    
    public Competencia desativar() {
        if (!ativo) return this;
        return new Competencia(id, perfilCandidatoId, titulo, descricao, nivel, false);
    }

    public UUID getId() {
        return id;
    }

    public UUID getPerfilCandidatoId() {
        return perfilCandidatoId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public NivelCompetencia getNivel() {
        return nivel;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
