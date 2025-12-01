package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "projeto_experiencia")
public class ProjetoExperienciaEntity extends AbstractAuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiencia_id", nullable = false)
    private ExperienciaProfissionalEntity experienciaProfissional;

    @Column(nullable = false, length = 80)
    private String titulo;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(nullable = false)
    private boolean ativo = true;

    public ProjetoExperienciaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ExperienciaProfissionalEntity getExperienciaProfissional() {
        return experienciaProfissional;
    }

    public void setExperienciaProfissional(ExperienciaProfissionalEntity experienciaProfissional) {
        this.experienciaProfissional = experienciaProfissional;
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
        ProjetoExperienciaEntity that = (ProjetoExperienciaEntity) o;
        return java.util.Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProjetoExperienciaEntity{" +
                "id=" + id +
                ", experienciaProfissional=" + (experienciaProfissional != null ? experienciaProfissional.getId() : null) +
                ", titulo='" + titulo + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}
