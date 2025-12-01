package com.barcelos.recrutamento.data.entity;

import com.barcelos.recrutamento.core.model.StatusCandidatura;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "candidatura", uniqueConstraints = {
        @UniqueConstraint(name = "uk_candidatura_vaga_usuario", columnNames = {"vaga_id", "candidato_usuario_id"})
})
public class CandidaturaEntity extends AbstractAuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_id", nullable = false)
    private VagaEntity vaga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidato_usuario_id", nullable = false)
    private UsuarioEntity candidato;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "status_candidatura", nullable = false, length = 20)
    private com.barcelos.recrutamento.core.model.StatusCandidatura status;

    @Column(name = "data_candidatura", nullable = false)
    private LocalDate dataCandidatura;

    @Column(name = "arquivo_curriculo", length = 255)
    private String arquivoCurriculo;

    @Column(precision = 5, scale = 2)
    private BigDecimal compatibilidade;

    public CandidaturaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public VagaEntity getVaga() {
        return vaga;
    }

    public void setVaga(VagaEntity vaga) {
        this.vaga = vaga;
    }

    public UsuarioEntity getCandidato() {
        return candidato;
    }

    public void setCandidato(UsuarioEntity candidato) {
        this.candidato = candidato;
    }

    public StatusCandidatura getStatus() {
        return status;
    }

    public void setStatus(StatusCandidatura status) {
        this.status = status;
    }

    public LocalDate getDataCandidatura() {
        return dataCandidatura;
    }

    public void setDataCandidatura(LocalDate dataCandidatura) {
        this.dataCandidatura = dataCandidatura;
    }

    public String getArquivoCurriculo() {
        return arquivoCurriculo;
    }

    public void setArquivoCurriculo(String arquivoCurriculo) {
        this.arquivoCurriculo = arquivoCurriculo;
    }

    public BigDecimal getCompatibilidade() {
        return compatibilidade;
    }

    public void setCompatibilidade(BigDecimal compatibilidade) {
        this.compatibilidade = compatibilidade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CandidaturaEntity that = (CandidaturaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CandidaturaEntity{" +
                "id=" + id +
                ", vaga=" + (vaga != null ? vaga.getId() : null) +
                ", candidato=" + (candidato != null ? candidato.getId() : null) +
                ", status=" + status +
                ", dataCandidatura=" + dataCandidatura +
                ", arquivoCurriculo='" + arquivoCurriculo + '\'' +
                ", compatibilidade=" + compatibilidade +
                '}';
    }
}
