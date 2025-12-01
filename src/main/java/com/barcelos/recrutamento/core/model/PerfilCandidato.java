package com.barcelos.recrutamento.core.model;

import com.barcelos.recrutamento.core.model.vo.Endereco;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class PerfilCandidato {
    private final UUID usuarioId;
    private final LocalDate dataNascimento;
    private final Endereco endereco;
    private final boolean ativo;

    private PerfilCandidato(UUID usuarioId, LocalDate dataNascimento, Endereco endereco, boolean ativo) {
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId must not be null");
        this.dataNascimento = Objects.requireNonNull(dataNascimento, "dataNascimento must not be null");
        this.endereco = Objects.requireNonNull(endereco, "endereco must not be null");
        this.ativo = ativo;
    }

    
    public static PerfilCandidato novo(UUID usuarioId, LocalDate dataNascimento, Endereco endereco) {
        return new PerfilCandidato(usuarioId, dataNascimento, endereco, true);
    }

    
    public static PerfilCandidato rehydrate(UUID id, UUID usuarioId, LocalDate dataNascimento, Endereco endereco, boolean ativo) {
        return new PerfilCandidato(usuarioId, dataNascimento, endereco, ativo);
    }

    
    public PerfilCandidato comDataNascimento(LocalDate novaDataNascimento) {
        return new PerfilCandidato(usuarioId, novaDataNascimento, endereco, ativo);
    }

    
    public PerfilCandidato comEndereco(Endereco novoEndereco) {
        return new PerfilCandidato(usuarioId, dataNascimento, novoEndereco, ativo);
    }

    
    public PerfilCandidato ativar() {
        if (ativo) return this;
        return new PerfilCandidato(usuarioId, dataNascimento, endereco, true);
    }

    
    public PerfilCandidato desativar() {
        if (!ativo) return this;
        return new PerfilCandidato(usuarioId, dataNascimento, endereco, false);
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
