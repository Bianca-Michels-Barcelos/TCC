package com.barcelos.recrutamento.core.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class VagaExterna {
    private final UUID id;
    private final String titulo;
    private final String descricao;
    private final String requisitos;
    private final String arquivoCurriculo;
    private final String conteudoCurriculo;
    private final ModeloCurriculoEnum modeloCurriculo;
    private final UUID candidatoUsuarioId;
    private final boolean ativo;
    private final LocalDateTime criadoEm;

    private VagaExterna(UUID id, String titulo, String descricao, String requisitos,
                        String arquivoCurriculo, String conteudoCurriculo, ModeloCurriculoEnum modeloCurriculo,
                        UUID candidatoUsuarioId, boolean ativo, LocalDateTime criadoEm) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.titulo = Objects.requireNonNull(titulo, "titulo must not be null");
        this.descricao = Objects.requireNonNull(descricao, "descricao must not be null");
        this.requisitos = Objects.requireNonNull(requisitos, "requisitos must not be null");
        this.arquivoCurriculo = arquivoCurriculo;
        this.conteudoCurriculo = conteudoCurriculo;
        this.modeloCurriculo = modeloCurriculo;
        this.candidatoUsuarioId = Objects.requireNonNull(candidatoUsuarioId, "candidatoUsuarioId must not be null");
        this.ativo = ativo;
        this.criadoEm = criadoEm;
    }

    
    public static VagaExterna nova(String titulo, String descricao, String requisitos, UUID candidatoUsuarioId) {
        return new VagaExterna(
            UUID.randomUUID(),
            titulo,
            descricao,
            requisitos,
            null,
            null,
            null,
            candidatoUsuarioId,
            true,
            LocalDateTime.now()
        );
    }

    
    public static VagaExterna rehydrate(UUID id, String titulo, String descricao, String requisitos,
                                        String arquivoCurriculo, String conteudoCurriculo, ModeloCurriculoEnum modeloCurriculo,
                                        UUID candidatoUsuarioId, boolean ativo, LocalDateTime criadoEm) {
        return new VagaExterna(id, titulo, descricao, requisitos, arquivoCurriculo, conteudoCurriculo, modeloCurriculo,
                              candidatoUsuarioId, ativo, criadoEm);
    }

    
    public VagaExterna comArquivoCurriculo(String novoArquivoCurriculo) {
        return new VagaExterna(id, titulo, descricao, requisitos, novoArquivoCurriculo, conteudoCurriculo, modeloCurriculo,
                              candidatoUsuarioId, ativo, criadoEm);
    }

    
    public VagaExterna comCurriculo(String novoConteudo, String novoArquivo, ModeloCurriculoEnum novoModelo) {
        return new VagaExterna(id, titulo, descricao, requisitos, novoArquivo, novoConteudo, novoModelo,
                              candidatoUsuarioId, ativo, criadoEm);
    }

    
    public VagaExterna atualizar(String novoTitulo, String novaDescricao, String novosRequisitos) {
        return new VagaExterna(
            id,
            novoTitulo != null ? novoTitulo : this.titulo,
            novaDescricao != null ? novaDescricao : this.descricao,
            novosRequisitos != null ? novosRequisitos : this.requisitos,
            arquivoCurriculo,
            conteudoCurriculo,
            modeloCurriculo,
            candidatoUsuarioId,
            ativo,
            criadoEm
        );
    }

    
    public VagaExterna desativar() {
        if (!ativo) {
            throw new IllegalStateException("Vaga externa já está desativada");
        }
        return new VagaExterna(id, titulo, descricao, requisitos, arquivoCurriculo, conteudoCurriculo, modeloCurriculo,
                              candidatoUsuarioId, false, criadoEm);
    }

    
    public VagaExterna reativar() {
        if (ativo) {
            throw new IllegalStateException("Vaga externa já está ativa");
        }
        return new VagaExterna(id, titulo, descricao, requisitos, arquivoCurriculo, conteudoCurriculo, modeloCurriculo,
                              candidatoUsuarioId, true, criadoEm);
    }

    
    public boolean possuiCurriculo() {
        return arquivoCurriculo != null && !arquivoCurriculo.isBlank();
    }

    
    public boolean isAtivo() {
        return ativo;
    }

    public UUID getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getRequisitos() {
        return requisitos;
    }

    public String getArquivoCurriculo() {
        return arquivoCurriculo;
    }

    public String getConteudoCurriculo() {
        return conteudoCurriculo;
    }

    public ModeloCurriculoEnum getModeloCurriculo() {
        return modeloCurriculo;
    }

    public UUID getCandidatoUsuarioId() {
        return candidatoUsuarioId;
    }

    public boolean getAtivo() {
        return ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
