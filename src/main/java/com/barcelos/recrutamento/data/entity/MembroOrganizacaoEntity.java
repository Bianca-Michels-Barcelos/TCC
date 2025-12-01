package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.UUID;

@Entity
@Table(name = "membro_organizacao")
@IdClass(MembroOrganizacaoId.class)
public class MembroOrganizacaoEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "organizacao_id", nullable = false)
    private UUID organizacaoId;

    @Id
    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "papel", nullable = false, length = 20)
    private PapelOrganizacao papel;

    @Column(nullable = false)
    private boolean ativo = true;

    public MembroOrganizacaoEntity() {
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(UUID organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public PapelOrganizacao getPapel() {
        return papel;
    }

    public void setPapel(PapelOrganizacao papel) {
        this.papel = papel;
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
        MembroOrganizacaoEntity that = (MembroOrganizacaoEntity) o;
        return java.util.Objects.equals(organizacaoId, that.organizacaoId) &&
                java.util.Objects.equals(usuarioId, that.usuarioId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(organizacaoId, usuarioId);
    }

    @Override
    public String toString() {
        return "MembroOrganizacaoEntity{" +
                "organizacaoId=" + organizacaoId +
                ", usuarioId=" + usuarioId +
                ", papel=" + papel +
                ", ativo=" + ativo +
                '}';
    }
}