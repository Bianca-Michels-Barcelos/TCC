package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "nivel_experiencia", uniqueConstraints = {
        @UniqueConstraint(name = "uk_nivel_experiencia_desc", columnNames = {"organizacao_id", "descricao"})
})
public class NivelExperienciaEntity extends AbstractAuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizacao_id", nullable = false)
    private OrganizacaoEntity organizacao;

    @Column(nullable = false, length = 50)
    private String descricao;

    @Column(nullable = false)
    private boolean ativo = true;

    public NivelExperienciaEntity() {
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
        if (o == null || getClass() != o.getClass()) return false;
        NivelExperienciaEntity that = (NivelExperienciaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
