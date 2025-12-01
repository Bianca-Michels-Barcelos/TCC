package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "historico_academico")
public class HistoricoAcademicoEntity extends AbstractAuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private PerfilCandidatoEntity perfilCandidato;

    @Column(nullable = false, length = 80)
    private String titulo;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(nullable = false, length = 80)
    private String instituicao;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(nullable = false)
    private boolean ativo = true;

    public HistoricoAcademicoEntity() {
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

    public String getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(String instituicao) {
        this.instituicao = instituicao;
    }

    public java.time.LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(java.time.LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public java.time.LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(java.time.LocalDate dataFim) {
        this.dataFim = dataFim;
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
        HistoricoAcademicoEntity that = (HistoricoAcademicoEntity) o;
        return java.util.Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

    @Override
    public String toString() {
        return "HistoricoAcademicoEntity{" +
                "id=" + id +
                ", perfilCandidato=" + (perfilCandidato != null ? perfilCandidato.getId() : null) +
                ", titulo='" + titulo + '\'' +
                ", instituicao='" + instituicao + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}
