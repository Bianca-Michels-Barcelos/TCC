package com.barcelos.recrutamento.core.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class ExperienciaProfissional {
    private final UUID id;
    private final UUID usuarioId;
    private final String cargo;
    private final String empresa;
    private final String descricao;
    private final LocalDate dataInicio;
    private final LocalDate dataFim;
    private final boolean ativo;

    private ExperienciaProfissional(UUID id, UUID usuarioId, String cargo, String empresa, String descricao, LocalDate dataInicio, LocalDate dataFim, boolean ativo) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId must not be null");
        if (cargo == null || cargo.isBlank()) throw new IllegalArgumentException("cargo must not be blank");
        if (cargo.length() > 80) throw new IllegalArgumentException("cargo max 80 chars");
        this.cargo = cargo;
        if (empresa == null || empresa.isBlank()) throw new IllegalArgumentException("empresa must not be blank");
        if (empresa.length() > 80) throw new IllegalArgumentException("empresa max 80 chars");
        this.empresa = empresa;
        if (descricao == null || descricao.isBlank()) throw new IllegalArgumentException("descricao must not be blank");
        this.descricao = descricao;
        this.dataInicio = Objects.requireNonNull(dataInicio, "dataInicio must not be null");
        if (dataFim != null && dataFim.isBefore(dataInicio))
            throw new IllegalArgumentException("dataFim must be after dataInicio");
        this.dataFim = dataFim;
        this.ativo = ativo;
    }

    
    public static ExperienciaProfissional novo(UUID id, UUID usuarioId, String cargo, String empresa, String descricao, LocalDate dataInicio, LocalDate dataFim) {
        return new ExperienciaProfissional(id, usuarioId, cargo, empresa, descricao, dataInicio, dataFim, true);
    }

    
    public static ExperienciaProfissional rehydrate(UUID id, UUID usuarioId, String cargo, String empresa, String descricao, LocalDate dataInicio, LocalDate dataFim, boolean ativo) {
        return new ExperienciaProfissional(id, usuarioId, cargo, empresa, descricao, dataInicio, dataFim, ativo);
    }

    
    public ExperienciaProfissional comCargo(String novoCargo) {
        return new ExperienciaProfissional(id, usuarioId, novoCargo, empresa, descricao, dataInicio, dataFim, ativo);
    }

    
    public ExperienciaProfissional comEmpresa(String novaEmpresa) {
        return new ExperienciaProfissional(id, usuarioId, cargo, novaEmpresa, descricao, dataInicio, dataFim, ativo);
    }

    
    public ExperienciaProfissional comDescricao(String novaDescricao) {
        return new ExperienciaProfissional(id, usuarioId, cargo, empresa, novaDescricao, dataInicio, dataFim, ativo);
    }

    
    public ExperienciaProfissional comDataInicio(LocalDate novaDataInicio) {
        return new ExperienciaProfissional(id, usuarioId, cargo, empresa, descricao, novaDataInicio, dataFim, ativo);
    }

    
    public ExperienciaProfissional comDataFim(LocalDate novaDataFim) {
        return new ExperienciaProfissional(id, usuarioId, cargo, empresa, descricao, dataInicio, novaDataFim, ativo);
    }

    
    public ExperienciaProfissional ativar() {
        if (ativo) return this;
        return new ExperienciaProfissional(id, usuarioId, cargo, empresa, descricao, dataInicio, dataFim, true);
    }

    
    public ExperienciaProfissional desativar() {
        if (!ativo) return this;
        return new ExperienciaProfissional(id, usuarioId, cargo, empresa, descricao, dataInicio, dataFim, false);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getCargo() {
        return cargo;
    }

    public String getEmpresa() {
        return empresa;
    }

    public String getDescricao() {
        return descricao;
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
