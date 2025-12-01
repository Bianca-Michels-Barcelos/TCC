package com.barcelos.recrutamento.core.model;

import java.util.Objects;
import java.util.UUID;

public final class BeneficioOrg {
    private final UUID id;
    private final UUID organizacaoId;
    private final String nome;
    private final String descricao;

    private BeneficioOrg(UUID id, UUID organizacaoId, String nome, String descricao) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.organizacaoId = Objects.requireNonNull(organizacaoId, "organizacaoId must not be null");
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("nome must not be blank");
        if (nome.length() > 80) throw new IllegalArgumentException("nome max 80 chars");
        this.nome = nome;
        if (descricao == null || descricao.isBlank()) throw new IllegalArgumentException("descricao must not be blank");
        this.descricao = descricao;
    }

    public static BeneficioOrg novo(UUID organizacaoId, String nome, String descricao) {
        return new BeneficioOrg(UUID.randomUUID(), organizacaoId, nome, descricao);
    }

    public static BeneficioOrg rehydrate(UUID id, UUID organizacaoId, String nome, String descricao) {
        return new BeneficioOrg(id, organizacaoId, nome, descricao);
    }

    public static BeneficioOrg atualizar(UUID id, UUID organizacaoId, String novoNome, String novaDescricao) {
        return new BeneficioOrg(id, organizacaoId, novoNome, novaDescricao);
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }
}
