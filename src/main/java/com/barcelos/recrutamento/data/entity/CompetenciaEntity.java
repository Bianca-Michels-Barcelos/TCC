package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "competencia")
public class CompetenciaEntity extends AbstractAuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private PerfilCandidatoEntity perfilCandidato;

    @Column(nullable = false, length = 50)
    private String titulo;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "nivel", columnDefinition = "nivel_competencia", nullable = false, length = 20)
    private NivelCompetencia nivel;

    @Column(nullable = false)
    private boolean ativo = true;

    public CompetenciaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PerfilCandidatoEntity getPerfilCandidato() {
        return perfilCandidato;
    }

    public void setPerfilCandidato(PerfilCandidatoEntity perfilCandidato) {
        this.perfilCandidato = perfilCandidato;
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

    public NivelCompetencia getNivel() {
        return nivel;
    }

    public void setNivel(NivelCompetencia nivel) {
        this.nivel = nivel;
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
        CompetenciaEntity that = (CompetenciaEntity) o;
        return java.util.Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CompetenciaEntity{" +
                "id=" + id +
                ", perfilCandidato=" + (perfilCandidato != null ? perfilCandidato.getId() : null) +
                ", titulo='" + titulo + '\'' +
                ", nivel=" + nivel +
                ", ativo=" + ativo +
                '}';
    }
}
