package com.barcelos.recrutamento.core.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Candidatura {
    private final UUID id;
    private final UUID vagaId;
    private final UUID candidatoUsuarioId;
    private final StatusCandidatura status;
    private final LocalDate dataCandidatura;
    private final String arquivoCurriculo;
    private final BigDecimal compatibilidade;

    private Candidatura(UUID id, UUID vagaId, UUID candidatoUsuarioId, StatusCandidatura status,
                        LocalDate dataCandidatura, String arquivoCurriculo, BigDecimal compatibilidade) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.vagaId = Objects.requireNonNull(vagaId, "vagaId must not be null");
        this.candidatoUsuarioId = Objects.requireNonNull(candidatoUsuarioId, "candidatoUsuarioId must not be null");
        if (status == null) throw new IllegalArgumentException("status must not be null");
        this.status = status;
        this.dataCandidatura = Objects.requireNonNull(dataCandidatura, "dataCandidatura must not be null");
        this.arquivoCurriculo = arquivoCurriculo;
        this.compatibilidade = compatibilidade;
    }

    
    public static Candidatura nova(UUID vagaId, UUID candidatoUsuarioId, String arquivoCurriculo) {
        return new Candidatura(
            UUID.randomUUID(),
            vagaId,
            candidatoUsuarioId,
            StatusCandidatura.PENDENTE,
            LocalDate.now(),
            arquivoCurriculo,
            null
        );
    }

    
    public static Candidatura rehydrate(UUID id, UUID vagaId, UUID candidatoUsuarioId,
                                        StatusCandidatura status, LocalDate dataCandidatura,
                                        String arquivoCurriculo, BigDecimal compatibilidade) {
        return new Candidatura(id, vagaId, candidatoUsuarioId, status, dataCandidatura,
                              arquivoCurriculo, compatibilidade);
    }

    
    public Candidatura comStatus(StatusCandidatura novoStatus) {
        if (novoStatus == null) {
            throw new IllegalArgumentException("Novo status não pode ser nulo");
        }
        return new Candidatura(id, vagaId, candidatoUsuarioId, novoStatus,
                              dataCandidatura, arquivoCurriculo, compatibilidade);
    }

    
    public Candidatura comCompatibilidade(BigDecimal novaCompatibilidade) {
        return new Candidatura(id, vagaId, candidatoUsuarioId, status,
                              dataCandidatura, arquivoCurriculo, novaCompatibilidade);
    }

    
    public Candidatura comArquivoCurriculo(String novoArquivoCurriculo) {
        return new Candidatura(id, vagaId, candidatoUsuarioId, status,
                              dataCandidatura, novoArquivoCurriculo, compatibilidade);
    }

    
    public Candidatura aceitar() {
        if (status == StatusCandidatura.REJEITADA) {
            throw new IllegalStateException("Não é possível aceitar uma candidatura já rejeitada");
        }
        return comStatus(StatusCandidatura.ACEITA);
    }

    
    public Candidatura rejeitar() {
        if (status == StatusCandidatura.ACEITA) {
            throw new IllegalStateException("Não é possível rejeitar uma candidatura já aceita");
        }
        return comStatus(StatusCandidatura.REJEITADA);
    }

    
    public Candidatura desistir() {
        if (status == StatusCandidatura.ACEITA) {
            throw new IllegalStateException("Não é possível desistir de uma candidatura já aceita");
        }
        if (status == StatusCandidatura.REJEITADA) {
            throw new IllegalStateException("Não é possível desistir de uma candidatura já rejeitada");
        }
        if (status == StatusCandidatura.DESISTENTE) {
            throw new IllegalStateException("Candidatura já está marcada como desistente");
        }
        return comStatus(StatusCandidatura.DESISTENTE);
    }

    
    public boolean isPendente() {
        return status == StatusCandidatura.PENDENTE;
    }

    
    public boolean isAceita() {
        return status == StatusCandidatura.ACEITA;
    }

    
    public boolean isRejeitada() {
        return status == StatusCandidatura.REJEITADA;
    }

    
    public boolean isDesistente() {
        return status == StatusCandidatura.DESISTENTE;
    }

    
    public boolean isEncerrada() {
        return status == StatusCandidatura.ACEITA ||
               status == StatusCandidatura.REJEITADA ||
               status == StatusCandidatura.DESISTENTE;
    }

    
    public boolean possuiCurriculo() {
        return arquivoCurriculo != null && !arquivoCurriculo.isBlank();
    }

    
    public boolean possuiCompatibilidadeCalculada() {
        return compatibilidade != null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getVagaId() {
        return vagaId;
    }

    public UUID getCandidatoUsuarioId() {
        return candidatoUsuarioId;
    }

    public StatusCandidatura getStatus() {
        return status;
    }

    public LocalDate getDataCandidatura() {
        return dataCandidatura;
    }

    public String getArquivoCurriculo() {
        return arquivoCurriculo;
    }

    public BigDecimal getCompatibilidade() {
        return compatibilidade;
    }
}
