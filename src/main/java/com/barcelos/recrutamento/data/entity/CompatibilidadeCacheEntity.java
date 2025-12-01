package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "compatibilidade_cache",
       uniqueConstraints = @UniqueConstraint(columnNames = {"candidato_usuario_id", "vaga_id"}),
       indexes = {
           @Index(name = "idx_cache_candidato", columnList = "candidato_usuario_id"),
           @Index(name = "idx_cache_vaga", columnList = "vaga_id"),
           @Index(name = "idx_cache_data_calculo", columnList = "data_calculo")
       })
public class CompatibilidadeCacheEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "candidato_usuario_id", nullable = false)
    private UUID candidatoUsuarioId;

    @Column(name = "vaga_id", nullable = false)
    private UUID vagaId;

    @Column(name = "percentual_compatibilidade", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualCompatibilidade;

    @Column(name = "justificativa", columnDefinition = "TEXT")
    private String justificativa;

    @Column(name = "data_calculo", nullable = false)
    private LocalDateTime dataCalculo;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    protected CompatibilidadeCacheEntity() {
    }

    public CompatibilidadeCacheEntity(UUID id, UUID candidatoUsuarioId, UUID vagaId,
                                     BigDecimal percentualCompatibilidade, String justificativa,
                                     LocalDateTime dataCalculo, LocalDateTime dataAtualizacao) {
        this.id = id;
        this.candidatoUsuarioId = candidatoUsuarioId;
        this.vagaId = vagaId;
        this.percentualCompatibilidade = percentualCompatibilidade;
        this.justificativa = justificativa;
        this.dataCalculo = dataCalculo;
        this.dataAtualizacao = dataAtualizacao;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCandidatoUsuarioId() {
        return candidatoUsuarioId;
    }

    public void setCandidatoUsuarioId(UUID candidatoUsuarioId) {
        this.candidatoUsuarioId = candidatoUsuarioId;
    }

    public UUID getVagaId() {
        return vagaId;
    }

    public void setVagaId(UUID vagaId) {
        this.vagaId = vagaId;
    }

    public BigDecimal getPercentualCompatibilidade() {
        return percentualCompatibilidade;
    }

    public void setPercentualCompatibilidade(BigDecimal percentualCompatibilidade) {
        this.percentualCompatibilidade = percentualCompatibilidade;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public LocalDateTime getDataCalculo() {
        return dataCalculo;
    }

    public void setDataCalculo(LocalDateTime dataCalculo) {
        this.dataCalculo = dataCalculo;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}

