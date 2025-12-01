package com.barcelos.recrutamento.data.entity;

import com.barcelos.recrutamento.core.model.ModalidadeTrabalho;
import com.barcelos.recrutamento.core.model.StatusVaga;
import com.barcelos.recrutamento.core.model.TipoContrato;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vaga")
public class VagaEntity extends AbstractAuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizacao_id", nullable = false)
    private OrganizacaoEntity organizacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recrutador_usuario_id", nullable = false)
    private UsuarioEntity recrutador;

    @Column(nullable = false, length = 80)
    private String titulo;

    @Column(nullable = false, columnDefinition = "text")
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String descricao;

    @Column(nullable = false, columnDefinition = "text")
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String requisitos;

    @Column(precision = 10, scale = 2)
    private BigDecimal salario;

    @Column(name = "data_publicacao", nullable = false)
    private LocalDate dataPublicacao;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "status_vaga", nullable = false, length = 20)
    private StatusVaga status;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tipo_contrato", columnDefinition = "tipo_contrato", nullable = false, length = 20)
    private TipoContrato tipoContrato;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "modalidade", columnDefinition = "modalidade_trabalho", nullable = false, length = 20)
    private ModalidadeTrabalho modalidade;

    @Column(name = "horario_trabalho", nullable = false, length = 30)
    private String horarioTrabalho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nivel_experiencia_id")
    private NivelExperienciaEntity nivelExperiencia;

    @Embedded
    private EnderecoSimplesEmbeddable endereco;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "motivo_cancelamento", columnDefinition = "TEXT")
    private String motivoCancelamento;

    public VagaEntity() {
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

    public UsuarioEntity getRecrutador() {
        return recrutador;
    }

    public void setRecrutador(UsuarioEntity recrutador) {
        this.recrutador = recrutador;
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

    public BigDecimal getSalario() {
        return salario;
    }

    public void setSalario(BigDecimal salario) {
        this.salario = salario;
    }

    public LocalDate getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(LocalDate dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public com.barcelos.recrutamento.core.model.StatusVaga getStatus() {
        return status;
    }

    public void setStatus(StatusVaga status) {
        this.status = status;
    }

    public com.barcelos.recrutamento.core.model.TipoContrato getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(TipoContrato tipoContrato) {
        this.tipoContrato = tipoContrato;
    }

    public com.barcelos.recrutamento.core.model.ModalidadeTrabalho getModalidade() {
        return modalidade;
    }

    public void setModalidade(ModalidadeTrabalho modalidade) {
        this.modalidade = modalidade;
    }

    public String getHorarioTrabalho() {
        return horarioTrabalho;
    }

    public void setHorarioTrabalho(String horarioTrabalho) {
        this.horarioTrabalho = horarioTrabalho;
    }

    public NivelExperienciaEntity getNivelExperiencia() {
        return nivelExperiencia;
    }

    public void setNivelExperiencia(NivelExperienciaEntity nivelExperiencia) {
        this.nivelExperiencia = nivelExperiencia;
    }

    public EnderecoSimplesEmbeddable getEndereco() {
        return endereco;
    }

    public void setEndereco(EnderecoSimplesEmbeddable endereco) {
        this.endereco = endereco;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }
}
