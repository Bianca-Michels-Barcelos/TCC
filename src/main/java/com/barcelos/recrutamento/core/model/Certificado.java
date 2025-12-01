package com.barcelos.recrutamento.core.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Certificado {
    private final UUID id;
    private final UUID perfilCandidatoId;
    private final String titulo;
    private final String instituicao;
    private final LocalDate dataEmissao;
    private final LocalDate dataValidade;
    private final String descricao;
    private final boolean ativo;

    private Certificado(UUID id, UUID perfilCandidatoId, String titulo, String instituicao,
                       LocalDate dataEmissao, LocalDate dataValidade, String descricao, boolean ativo) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.perfilCandidatoId = Objects.requireNonNull(perfilCandidatoId, "perfilCandidatoId must not be null");
        if (titulo == null || titulo.isBlank()) throw new IllegalArgumentException("titulo must not be blank");
        if (titulo.length() > 100) throw new IllegalArgumentException("titulo max 100 chars");
        this.titulo = titulo;
        if (instituicao == null || instituicao.isBlank()) throw new IllegalArgumentException("instituicao must not be blank");
        if (instituicao.length() > 100) throw new IllegalArgumentException("instituicao max 100 chars");
        this.instituicao = instituicao;
        this.dataEmissao = Objects.requireNonNull(dataEmissao, "dataEmissao must not be null");
        if (dataValidade != null && dataValidade.isBefore(dataEmissao)) {
            throw new IllegalArgumentException("dataValidade must be after dataEmissao");
        }
        this.dataValidade = dataValidade;

        this.descricao = descricao;
        this.ativo = ativo;
    }

    
    public static Certificado novo(UUID perfilCandidatoId, String titulo, String instituicao,
                                   LocalDate dataEmissao, LocalDate dataValidade, String descricao) {
        return new Certificado(UUID.randomUUID(), perfilCandidatoId, titulo, instituicao,
                              dataEmissao, dataValidade, descricao, true);
    }

    
    public static Certificado rehydrate(UUID id, UUID perfilCandidatoId, String titulo, String instituicao,
                                       LocalDate dataEmissao, LocalDate dataValidade, String descricao, boolean ativo) {
        return new Certificado(id, perfilCandidatoId, titulo, instituicao, dataEmissao, dataValidade, descricao, ativo);
    }

    
    public Certificado comTitulo(String novoTitulo) {
        return new Certificado(id, perfilCandidatoId, novoTitulo, instituicao, dataEmissao, dataValidade, descricao, ativo);
    }

    
    public Certificado comInstituicao(String novaInstituicao) {
        return new Certificado(id, perfilCandidatoId, titulo, novaInstituicao, dataEmissao, dataValidade, descricao, ativo);
    }

    
    public Certificado comDataEmissao(LocalDate novaDataEmissao) {
        return new Certificado(id, perfilCandidatoId, titulo, instituicao, novaDataEmissao, dataValidade, descricao, ativo);
    }

    
    public Certificado comDataValidade(LocalDate novaDataValidade) {
        return new Certificado(id, perfilCandidatoId, titulo, instituicao, dataEmissao, novaDataValidade, descricao, ativo);
    }

    
    public Certificado comDescricao(String novaDescricao) {
        return new Certificado(id, perfilCandidatoId, titulo, instituicao, dataEmissao, dataValidade, novaDescricao, ativo);
    }

    
    public Certificado ativar() {
        if (ativo) return this;
        return new Certificado(id, perfilCandidatoId, titulo, instituicao, dataEmissao, dataValidade, descricao, true);
    }

    
    public Certificado desativar() {
        if (!ativo) return this;
        return new Certificado(id, perfilCandidatoId, titulo, instituicao, dataEmissao, dataValidade, descricao, false);
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

    public String getInstituicao() {
        return instituicao;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
