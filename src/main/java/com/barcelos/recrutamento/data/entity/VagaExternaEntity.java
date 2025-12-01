package com.barcelos.recrutamento.data.entity;

import com.barcelos.recrutamento.core.model.ModeloCurriculoEnum;
import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "vaga_externa")
public class VagaExternaEntity extends AbstractAuditableEntity {
    @Id
    private UUID id;

    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "requisitos", nullable = false, columnDefinition = "TEXT")
    private String requisitos;

    @Column(name = "arquivo_curriculo", columnDefinition = "TEXT")
    private String arquivoCurriculo;

    @Column(name = "conteudo_curriculo", columnDefinition = "TEXT")
    private String conteudoCurriculo;

    @Enumerated(EnumType.STRING)
    @Column(name = "modelo_curriculo", length = 20)
    private ModeloCurriculoEnum modeloCurriculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private PerfilCandidatoEntity candidato;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    public VagaExternaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getRequisitos() {
        return requisitos;
    }

    public void setRequisitos(String requisitos) {
        this.requisitos = requisitos;
    }

    public String getArquivoCurriculo() {
        return arquivoCurriculo;
    }

    public void setArquivoCurriculo(String arquivoCurriculo) {
        this.arquivoCurriculo = arquivoCurriculo;
    }

    public String getConteudoCurriculo() {
        return conteudoCurriculo;
    }

    public void setConteudoCurriculo(String conteudoCurriculo) {
        this.conteudoCurriculo = conteudoCurriculo;
    }

    public ModeloCurriculoEnum getModeloCurriculo() {
        return modeloCurriculo;
    }

    public void setModeloCurriculo(ModeloCurriculoEnum modeloCurriculo) {
        this.modeloCurriculo = modeloCurriculo;
    }

    public PerfilCandidatoEntity getCandidato() {
        return candidato;
    }

    public void setCandidato(PerfilCandidatoEntity candidato) {
        this.candidato = candidato;
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
        VagaExternaEntity that = (VagaExternaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "VagaExternaEntity{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", candidato=" + (candidato != null ? candidato.getId() : null) +
                ", arquivoCurriculo='" + arquivoCurriculo + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}
