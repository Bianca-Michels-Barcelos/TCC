package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "beneficio_org", uniqueConstraints = {
        @UniqueConstraint(name = "uk_beneficio_org_titulo", columnNames = {"organizacao_id", "titulo"})
})
public class BeneficioOrgEntity extends AbstractAuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizacao_id", nullable = false)
    private OrganizacaoEntity organizacao;

    @Column(nullable = false, length = 50)
    private String titulo;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(nullable = false)
    private boolean ativo = true;

    public BeneficioOrgEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OrganizacaoEntity getOrganizacao() {
        return organizacao;
    }

    public void setOrganizacao(OrganizacaoEntity organizacao) {
        this.organizacao = organizacao;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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
        BeneficioOrgEntity that = (BeneficioOrgEntity) o;
        return java.util.Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BeneficioOrgEntity{" +
                "id=" + id +
                ", organizacao=" + (organizacao != null ? organizacao.getId() : null) +
                ", titulo='" + titulo + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}
