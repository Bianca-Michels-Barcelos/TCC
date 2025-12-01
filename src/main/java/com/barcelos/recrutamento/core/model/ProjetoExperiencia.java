package com.barcelos.recrutamento.core.model;

import java.util.Objects;
import java.util.UUID;

public final class ProjetoExperiencia {
    private final UUID id;
    private final UUID experienciaProfissionalId;
    private final String nome;
    private final String descricao;
    private final boolean ativo;

    private ProjetoExperiencia(UUID id, UUID experienciaProfissionalId, String nome, String descricao, boolean ativo) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.experienciaProfissionalId = Objects.requireNonNull(experienciaProfissionalId, "experienciaProfissionalId must not be null");
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("nome must not be blank");
        if (nome.length() > 80) throw new IllegalArgumentException("nome max 80 chars");
        this.nome = nome;
        if (descricao == null || descricao.isBlank()) throw new IllegalArgumentException("descricao must not be blank");
        this.descricao = descricao;
        this.ativo = ativo;
    }

    
    public static ProjetoExperiencia novo(UUID id, UUID experienciaProfissionalId, String nome, String descricao) {
        return new ProjetoExperiencia(id, experienciaProfissionalId, nome, descricao, true);
    }

    
    public static ProjetoExperiencia rehydrate(UUID id, UUID experienciaProfissionalId, String nome, String descricao, boolean ativo) {
        return new ProjetoExperiencia(id, experienciaProfissionalId, nome, descricao, ativo);
    }

    
    public ProjetoExperiencia comNome(String novoNome) {
        return new ProjetoExperiencia(id, experienciaProfissionalId, novoNome, descricao, ativo);
    }

    
    public ProjetoExperiencia comDescricao(String novaDescricao) {
        return new ProjetoExperiencia(id, experienciaProfissionalId, nome, novaDescricao, ativo);
    }

    
    public ProjetoExperiencia ativar() {
        if (ativo) return this;
        return new ProjetoExperiencia(id, experienciaProfissionalId, nome, descricao, true);
    }

    
    public ProjetoExperiencia desativar() {
        if (!ativo) return this;
        return new ProjetoExperiencia(id, experienciaProfissionalId, nome, descricao, false);
    }

    public UUID getId() {
        return id;
    }

    public UUID getExperienciaProfissionalId() {
        return experienciaProfissionalId;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
