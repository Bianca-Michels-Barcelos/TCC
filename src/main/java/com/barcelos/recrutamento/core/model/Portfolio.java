package com.barcelos.recrutamento.core.model;

import java.util.Objects;
import java.util.UUID;

public final class Portfolio {
    private final UUID id;
    private final UUID usuarioId;
    private final String titulo;
    private final String link;
    private final boolean ativo;

    private Portfolio(UUID id, UUID usuarioId, String titulo, String link, boolean ativo) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId must not be null");
        if (titulo == null || titulo.isBlank()) throw new IllegalArgumentException("titulo must not be blank");
        if (titulo.length() > 100) throw new IllegalArgumentException("titulo max 100 chars");
        this.titulo = titulo;
        if (link == null || link.isBlank()) throw new IllegalArgumentException("link must not be blank");
        if (link.length() > 255) throw new IllegalArgumentException("link max 255 chars");
        this.link = link;
        this.ativo = ativo;
    }

    
    public static Portfolio novo(UUID usuarioId, String titulo, String link) {
        return new Portfolio(UUID.randomUUID(), usuarioId, titulo, link, true);
    }

    
    public static Portfolio rehydrate(UUID id, UUID usuarioId, String titulo, String link, boolean ativo) {
        return new Portfolio(id, usuarioId, titulo, link, ativo);
    }

    
    public Portfolio comTitulo(String novoTitulo) {
        return new Portfolio(id, usuarioId, novoTitulo, link, ativo);
    }

    
    public Portfolio comLink(String novoLink) {
        return new Portfolio(id, usuarioId, titulo, novoLink, ativo);
    }

    
    public Portfolio ativar() {
        if (ativo) return this;
        return new Portfolio(id, usuarioId, titulo, link, true);
    }

    
    public Portfolio desativar() {
        if (!ativo) return this;
        return new Portfolio(id, usuarioId, titulo, link, false);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getLink() {
        return link;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
