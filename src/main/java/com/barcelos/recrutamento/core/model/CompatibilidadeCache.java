package com.barcelos.recrutamento.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class CompatibilidadeCache {
    private final UUID id;
    private final UUID candidatoUsuarioId;
    private final UUID vagaId;
    private final BigDecimal percentualCompatibilidade;
    private final String justificativa;
    private final LocalDateTime dataCalculo;
    private final LocalDateTime dataAtualizacao;

    private CompatibilidadeCache(UUID id, UUID candidatoUsuarioId, UUID vagaId,
                                 BigDecimal percentualCompatibilidade, String justificativa,
                                 LocalDateTime dataCalculo, LocalDateTime dataAtualizacao) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.candidatoUsuarioId = Objects.requireNonNull(candidatoUsuarioId, "candidatoUsuarioId must not be null");
        this.vagaId = Objects.requireNonNull(vagaId, "vagaId must not be null");
        this.percentualCompatibilidade = Objects.requireNonNull(percentualCompatibilidade, "percentualCompatibilidade must not be null");
        this.justificativa = justificativa;
        this.dataCalculo = Objects.requireNonNull(dataCalculo, "dataCalculo must not be null");
        this.dataAtualizacao = dataAtualizacao;
    }

    
    public static CompatibilidadeCache novo(UUID candidatoUsuarioId, UUID vagaId,
                                           BigDecimal percentualCompatibilidade, String justificativa) {
        return new CompatibilidadeCache(
            UUID.randomUUID(),
            candidatoUsuarioId,
            vagaId,
            percentualCompatibilidade,
            justificativa,
            LocalDateTime.now(),
            null
        );
    }

    
    public static CompatibilidadeCache rehydrate(UUID id, UUID candidatoUsuarioId, UUID vagaId,
                                                 BigDecimal percentualCompatibilidade, String justificativa,
                                                 LocalDateTime dataCalculo, LocalDateTime dataAtualizacao) {
        return new CompatibilidadeCache(id, candidatoUsuarioId, vagaId, percentualCompatibilidade,
                                        justificativa, dataCalculo, dataAtualizacao);
    }

    
    public CompatibilidadeCache atualizar(BigDecimal novoPercentual, String novaJustificativa) {
        return new CompatibilidadeCache(
            id,
            candidatoUsuarioId,
            vagaId,
            novoPercentual,
            novaJustificativa,
            dataCalculo,
            LocalDateTime.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getCandidatoUsuarioId() {
        return candidatoUsuarioId;
    }

    public UUID getVagaId() {
        return vagaId;
    }

    public BigDecimal getPercentualCompatibilidade() {
        return percentualCompatibilidade;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public LocalDateTime getDataCalculo() {
        return dataCalculo;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    
    public boolean foiAtualizado() {
        return dataAtualizacao != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompatibilidadeCache that = (CompatibilidadeCache) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CompatibilidadeCache{" +
                "id=" + id +
                ", candidatoUsuarioId=" + candidatoUsuarioId +
                ", vagaId=" + vagaId +
                ", percentualCompatibilidade=" + percentualCompatibilidade +
                ", dataCalculo=" + dataCalculo +
                '}';
    }
}
