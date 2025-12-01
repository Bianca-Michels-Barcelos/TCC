package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "experiencia_profissional")
public class ExperienciaProfissionalEntity extends AbstractAuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private PerfilCandidatoEntity perfilCandidato;

    @Column(nullable = false, length = 80)
    private String cargo;

    @Column(nullable = false, length = 80)
    private String empresa;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(nullable = false)
    private boolean ativo = true;

    public ExperienciaProfissionalEntity() {
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

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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
        ExperienciaProfissionalEntity that = (ExperienciaProfissionalEntity) o;
        return java.util.Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ExperienciaProfissionalEntity{" +
                "id=" + id +
                ", perfilCandidato=" + (perfilCandidato != null ? perfilCandidato.getId() : null) +
                ", cargo='" + cargo + '\'' +
                ", empresa='" + empresa + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}
