package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "organizacao", uniqueConstraints = {
        @UniqueConstraint(name = "uk_organizacao_cnpj", columnNames = "cnpj")
})
public class OrganizacaoEntity extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 14, unique = true)
    private String cnpj;

    @Column(nullable = false, length = 100)
    private String nome;

    @Embedded
    private EnderecoEmbeddable endereco;

    @Column(nullable = false)
    private boolean ativo = true;

    public OrganizacaoEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public EnderecoEmbeddable getEndereco() {
        return endereco;
    }

    public void setEndereco(EnderecoEmbeddable endereco) {
        this.endereco = endereco;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganizacaoEntity that = (OrganizacaoEntity) o;
        return java.util.Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OrganizacaoEntity{" +
                "id=" + id +
                ", cnpj='" + cnpj + '\'' +
                ", nome='" + nome + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}
