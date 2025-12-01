package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processo_seletivo")
public class ProcessoSeletivoEntity {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidatura_id", nullable = false, unique = true)
    private CandidaturaEntity candidatura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etapa_processo_atual_id", nullable = false)
    private EtapaProcessoEntity etapaProcessoAtual;

    @Column(name = "data_inicio", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim", columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime dataFim;

    @Column(name = "data_ultima_mudanca", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime dataUltimaMudanca;

    public ProcessoSeletivoEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CandidaturaEntity getCandidatura() {
        return candidatura;
    }

    public void setCandidatura(CandidaturaEntity candidatura) {
        this.candidatura = candidatura;
    }

    public EtapaProcessoEntity getEtapaProcessoAtual() {
        return etapaProcessoAtual;
    }

    public void setEtapaProcessoAtual(EtapaProcessoEntity etapaProcessoAtual) {
        this.etapaProcessoAtual = etapaProcessoAtual;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public LocalDateTime getDataUltimaMudanca() {
        return dataUltimaMudanca;
    }

    public void setDataUltimaMudanca(LocalDateTime dataUltimaMudanca) {
        this.dataUltimaMudanca = dataUltimaMudanca;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessoSeletivoEntity that = (ProcessoSeletivoEntity) o;
        return java.util.Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProcessoSeletivoEntity{" +
                "id=" + id +
                ", candidatura=" + (candidatura != null ? candidatura.getId() : null) +
                ", etapaProcessoAtual=" + (etapaProcessoAtual != null ? etapaProcessoAtual.getId() : null) +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                '}';
    }
}
