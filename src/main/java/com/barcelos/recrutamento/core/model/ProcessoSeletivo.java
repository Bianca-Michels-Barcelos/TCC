package com.barcelos.recrutamento.core.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class ProcessoSeletivo {
    private final UUID id;
    private final UUID candidaturaId;
    private final UUID etapaProcessoAtualId;
    private final LocalDateTime dataInicio;
    private final LocalDateTime dataFim;
    private final LocalDateTime dataUltimaMudanca;

    private ProcessoSeletivo(UUID id, UUID candidaturaId, UUID etapaProcessoAtualId, LocalDateTime dataInicio,
                            LocalDateTime dataFim, LocalDateTime dataUltimaMudanca) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.candidaturaId = Objects.requireNonNull(candidaturaId, "candidaturaId must not be null");
        this.etapaProcessoAtualId = Objects.requireNonNull(etapaProcessoAtualId, "etapaProcessoAtualId must not be null");
        this.dataInicio = Objects.requireNonNull(dataInicio, "dataInicio must not be null");
        this.dataFim = dataFim;
        this.dataUltimaMudanca = Objects.requireNonNull(dataUltimaMudanca, "dataUltimaMudanca must not be null");
    }

    
    public static ProcessoSeletivo novo(UUID candidaturaId, UUID etapaProcessoInicialId) {
        LocalDateTime agora = LocalDateTime.now();
        return new ProcessoSeletivo(
            UUID.randomUUID(),
            candidaturaId,
            etapaProcessoInicialId,
            agora,
            null,
            agora
        );
    }

    
    public static ProcessoSeletivo rehydrate(UUID id, UUID candidaturaId, UUID etapaProcessoAtualId,
                                            LocalDateTime dataInicio, LocalDateTime dataFim,
                                            LocalDateTime dataUltimaMudanca) {
        return new ProcessoSeletivo(id, candidaturaId, etapaProcessoAtualId, dataInicio, dataFim, dataUltimaMudanca);
    }

    
    public ProcessoSeletivo comEtapaAtual(UUID novaEtapaProcessoId) {
        if (novaEtapaProcessoId == null) {
            throw new IllegalArgumentException("Nova etapa não pode ser nula");
        }
        return new ProcessoSeletivo(id, candidaturaId, novaEtapaProcessoId, dataInicio, dataFim, LocalDateTime.now());
    }

    
    public ProcessoSeletivo comDataFim(LocalDateTime novaDataFim) {
        return new ProcessoSeletivo(id, candidaturaId, etapaProcessoAtualId, dataInicio, novaDataFim, dataUltimaMudanca);
    }

    
    public ProcessoSeletivo finalizar() {
        if (dataFim != null) {
            throw new IllegalStateException("Processo seletivo já foi finalizado");
        }
        return comDataFim(LocalDateTime.now());
    }

    
    public ProcessoSeletivo avancarParaEtapa(UUID proximaEtapaProcessoId) {
        if (dataFim != null) {
            throw new IllegalStateException("Não é possível avançar um processo já finalizado");
        }
        return comEtapaAtual(proximaEtapaProcessoId);
    }

    
    public boolean isEmAndamento() {
        return dataFim == null;
    }

    
    public boolean isFinalizado() {
        return dataFim != null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCandidaturaId() {
        return candidaturaId;
    }

    
    public UUID getEtapaProcessoAtualId() {
        return etapaProcessoAtualId;
    }

    
    @Deprecated
    public UUID getEtapaAtualId() {
        return etapaProcessoAtualId;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public LocalDateTime getDataUltimaMudanca() {
        return dataUltimaMudanca;
    }
}
