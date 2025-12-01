package com.barcelos.recrutamento.core.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class HistoricoAcademico {
    private final UUID id;
    private final UUID usuarioId;
    private final String titulo;
    private final String descricao;
    private final String instituicao;
    private final LocalDate dataInicio;
    private final LocalDate dataFim;
    private final boolean ativo;

    private HistoricoAcademico(UUID id, UUID usuarioId, String titulo, String descricao, String instituicao, LocalDate dataInicio, LocalDate dataFim, boolean ativo) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId must not be null");
        if (titulo == null || titulo.isBlank()) throw new IllegalArgumentException("titulo must not be blank");
        if (titulo.length() > 80) throw new IllegalArgumentException("titulo max 80 chars");
        this.titulo = titulo;

        this.descricao = descricao;
        if (instituicao == null || instituicao.isBlank())
            throw new IllegalArgumentException("instituicao must not be blank");
        if (instituicao.length() > 80) throw new IllegalArgumentException("instituicao max 80 chars");
        this.instituicao = instituicao;
        this.dataInicio = Objects.requireNonNull(dataInicio, "dataInicio must not be null");
        if (dataFim != null && dataFim.isBefore(dataInicio))
            throw new IllegalArgumentException("dataFim must be after dataInicio");
        this.dataFim = dataFim;
        this.ativo = ativo;
    }

    
    public static HistoricoAcademico novo(UUID id, UUID usuarioId, String titulo, String descricao, String instituicao, LocalDate dataInicio, LocalDate dataFim) {
        return new HistoricoAcademico(id, usuarioId, titulo, descricao, instituicao, dataInicio, dataFim, true);
    }

    
    public static HistoricoAcademico rehydrate(UUID id, UUID usuarioId, String titulo, String descricao, String instituicao, LocalDate dataInicio, LocalDate dataFim, boolean ativo) {
        return new HistoricoAcademico(id, usuarioId, titulo, descricao, instituicao, dataInicio, dataFim, ativo);
    }

    
    public HistoricoAcademico comTitulo(String novoTitulo) {
        return new HistoricoAcademico(id, usuarioId, novoTitulo, descricao, instituicao, dataInicio, dataFim, ativo);
    }

    
    public HistoricoAcademico comDescricao(String novaDescricao) {
        return new HistoricoAcademico(id, usuarioId, titulo, novaDescricao, instituicao, dataInicio, dataFim, ativo);
    }

    
    public HistoricoAcademico comInstituicao(String novaInstituicao) {
        return new HistoricoAcademico(id, usuarioId, titulo, descricao, novaInstituicao, dataInicio, dataFim, ativo);
    }

    
    public HistoricoAcademico comDataInicio(LocalDate novaDataInicio) {
        return new HistoricoAcademico(id, usuarioId, titulo, descricao, instituicao, novaDataInicio, dataFim, ativo);
    }

    
    public HistoricoAcademico comDataFim(LocalDate novaDataFim) {
        return new HistoricoAcademico(id, usuarioId, titulo, descricao, instituicao, dataInicio, novaDataFim, ativo);
    }

    
    public HistoricoAcademico ativar() {
        if (ativo) return this;
        return new HistoricoAcademico(id, usuarioId, titulo, descricao, instituicao, dataInicio, dataFim, true);
    }

    
    public HistoricoAcademico desativar() {
        if (!ativo) return this;
        return new HistoricoAcademico(id, usuarioId, titulo, descricao, instituicao, dataInicio, dataFim, false);
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

    public String getDescricao() {
        return descricao;
    }

    public String getInstituicao() {
        return instituicao;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
