package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "historico_etapa_processo")
public class HistoricoEtapaProcessoEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id", nullable = false)
    private ProcessoSeletivoEntity processo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etapa_anterior_id")
    private EtapaProcessoEntity etapaAnterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etapa_nova_id", nullable = false)
    private EtapaProcessoEntity etapaNova;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "data_mudanca", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime dataMudanca;

    @Column(name = "criado_em", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        if (dataMudanca == null) {
            dataMudanca = LocalDateTime.now();
        }
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public HistoricoEtapaProcessoEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ProcessoSeletivoEntity getProcesso() {
        return processo;
    }

    public void setProcesso(ProcessoSeletivoEntity processo) {
        this.processo = processo;
    }

    public EtapaProcessoEntity getEtapaAnterior() {
        return etapaAnterior;
    }

    public void setEtapaAnterior(EtapaProcessoEntity etapaAnterior) {
        this.etapaAnterior = etapaAnterior;
    }

    public EtapaProcessoEntity getEtapaNova() {
        return etapaNova;
    }

    public void setEtapaNova(EtapaProcessoEntity etapaNova) {
        this.etapaNova = etapaNova;
    }

    public UsuarioEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioEntity usuario) {
        this.usuario = usuario;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public LocalDateTime getDataMudanca() {
        return dataMudanca;
    }

    public void setDataMudanca(LocalDateTime dataMudanca) {
        this.dataMudanca = dataMudanca;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoricoEtapaProcessoEntity that = (HistoricoEtapaProcessoEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "HistoricoEtapaProcessoEntity{" +
                "id=" + id +
                ", processo=" + (processo != null ? processo.getId() : null) +
                ", etapaAnterior=" + (etapaAnterior != null ? etapaAnterior.getId() : null) +
                ", etapaNova=" + (etapaNova != null ? etapaNova.getId() : null) +
                ", usuario=" + (usuario != null ? usuario.getId() : null) +
                ", dataMudanca=" + dataMudanca +
                '}';
    }
}
