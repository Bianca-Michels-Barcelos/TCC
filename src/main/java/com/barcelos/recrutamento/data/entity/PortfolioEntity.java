package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "portfolio")
public class PortfolioEntity extends AbstractAuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private PerfilCandidatoEntity perfilCandidato;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false, length = 255)
    private String link;

    @Column(nullable = false)
    private boolean ativo = true;

    public PortfolioEntity() {
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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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
        PortfolioEntity that = (PortfolioEntity) o;
        return java.util.Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PortfolioEntity{" +
                "id=" + id +
                ", perfilCandidato=" + (perfilCandidato != null ? perfilCandidato.getId() : null) +
                ", titulo='" + titulo + '\'' +
                ", link='" + link + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}
