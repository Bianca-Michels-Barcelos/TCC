package com.barcelos.recrutamento.core.model;

import com.barcelos.recrutamento.core.model.vo.Cpf;
import com.barcelos.recrutamento.core.model.vo.Email;

import java.util.Objects;
import java.util.UUID;

public final class Usuario {
    private final UUID id;
    private final String nome;
    private final Email email;
    private final Cpf cpf;
    private final String senhaHash;
    private final boolean emailVerificado;
    private final boolean ativo;

    private Usuario(UUID id, String nome, Email email, Cpf cpf, String senhaHash, boolean emailVerificado, boolean ativo) {
        this.id = id;
        this.nome = Objects.requireNonNull(nome, "nome must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.cpf = Objects.requireNonNull(cpf, "cpf must not be null");
        this.senhaHash = Objects.requireNonNull(senhaHash, "senhaHash must not be null");
        this.emailVerificado = emailVerificado;
        this.ativo = ativo;
    }

    
    public static Usuario novo(String nome, Email email, Cpf cpf, String senhaHash) {
        return new Usuario(null, nome, email, cpf, senhaHash, false, true);
    }

    
    public static Usuario rehydrate(UUID id, String nome, Email email, Cpf cpf, String senhaHash, boolean emailVerificado, boolean ativo) {
        return new Usuario(Objects.requireNonNull(id), nome, email, cpf, senhaHash, emailVerificado, ativo);
    }

    
    public Usuario comNome(String novoNome) {
        return new Usuario(id, novoNome, email, cpf, senhaHash, emailVerificado, ativo);
    }

    
    public Usuario comEmail(Email novoEmail) {
        if (email.equals(novoEmail)) return this;
        return new Usuario(id, nome, novoEmail, cpf, senhaHash, false, ativo);
    }

    
    public Usuario comSenhaHash(String novoSenhaHash) {
        return new Usuario(id, nome, email, cpf, novoSenhaHash, emailVerificado, ativo);
    }

    
    public Usuario verificarEmail() {
        if (emailVerificado) return this;
        return new Usuario(id, nome, email, cpf, senhaHash, true, ativo);
    }

    
    public Usuario ativar() {
        if (ativo) return this;
        return new Usuario(id, nome, email, cpf, senhaHash, emailVerificado, true);
    }

    
    public Usuario desativar() {
        if (!ativo) return this;
        return new Usuario(id, nome, email, cpf, senhaHash, emailVerificado, false);
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Email getEmail() {
        return email;
    }

    public Cpf getCpf() {
        return cpf;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public boolean isEmailVerificado() {
        return emailVerificado;
    }

    public boolean isAtivo() {
        return ativo;
    }
}