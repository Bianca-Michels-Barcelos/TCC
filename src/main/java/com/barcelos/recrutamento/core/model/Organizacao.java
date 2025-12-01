package com.barcelos.recrutamento.core.model;

import com.barcelos.recrutamento.core.model.vo.Cnpj;
import com.barcelos.recrutamento.core.model.vo.Endereco;

import java.util.Objects;
import java.util.UUID;

public final class Organizacao {
    private final UUID id;
    private final Cnpj cnpj;
    private final String nome;
    private final Endereco endereco;
    private final boolean ativo;

    private Organizacao(UUID id, Cnpj cnpj, String nome, Endereco endereco, boolean ativo) {
        this.id = id;
        this.cnpj = Objects.requireNonNull(cnpj, "cnpj must not be null");
        this.nome = Objects.requireNonNull(nome, "nome must not be null");
        this.endereco = Objects.requireNonNull(endereco, "endereco must not be null");
        this.ativo = ativo;
    }

    
    public static Organizacao novo(Cnpj cnpj, String nome, Endereco endereco) {
        return new Organizacao(null, Objects.requireNonNull(cnpj, "cnpj must not be null"),
                Objects.requireNonNull(nome, "nome must not be null"),
                Objects.requireNonNull(endereco, "endereco must not be null"), true);
    }

    
    public static Organizacao rehydrate(UUID id, Cnpj cnpj, String nome, Endereco endereco, boolean ativo) {
        return new Organizacao(Objects.requireNonNull(id, "id é obrigatório na rehidratação"),
                cnpj, nome, endereco, ativo);
    }

    
    public Organizacao comNome(String novoNome) {
        return new Organizacao(id, cnpj, novoNome, endereco, ativo);
    }

    
    public Organizacao comEndereco(Endereco novoEndereco) {
        return new Organizacao(id, cnpj, nome, novoEndereco, ativo);
    }

    
    public Organizacao ativar() {
        if (ativo) return this;
        return new Organizacao(id, cnpj, nome, endereco, true);
    }

    
    public Organizacao desativar() {
        if (!ativo) return this;
        return new Organizacao(id, cnpj, nome, endereco, false);
    }

    public UUID getId() {
        return id;
    }

    public Cnpj getCnpj() {
        return cnpj;
    }

    public String getNome() {
        return nome;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
