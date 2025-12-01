package com.barcelos.recrutamento.core.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class ConviteProcessoSeletivo {

    private final UUID id;
    private final UUID vagaId;
    private final UUID recrutadorUsuarioId;
    private final UUID candidatoUsuarioId;
    private final String mensagem;
    private final StatusConviteProcesso status;
    private final LocalDateTime dataEnvio;
    private final LocalDateTime dataExpiracao;
    private final LocalDateTime dataResposta;

    private ConviteProcessoSeletivo(
            UUID id,
            UUID vagaId,
            UUID recrutadorUsuarioId,
            UUID candidatoUsuarioId,
            String mensagem,
            StatusConviteProcesso status,
            LocalDateTime dataEnvio,
            LocalDateTime dataExpiracao,
            LocalDateTime dataResposta
    ) {
        this.id = Objects.requireNonNull(id, "id não pode ser nulo");
        this.vagaId = Objects.requireNonNull(vagaId, "vagaId não pode ser nulo");
        this.recrutadorUsuarioId = Objects.requireNonNull(recrutadorUsuarioId, "recrutadorUsuarioId não pode ser nulo");
        this.candidatoUsuarioId = Objects.requireNonNull(candidatoUsuarioId, "candidatoUsuarioId não pode ser nulo");
        this.mensagem = mensagem;
        this.status = Objects.requireNonNull(status, "status não pode ser nulo");
        this.dataEnvio = Objects.requireNonNull(dataEnvio, "dataEnvio não pode ser nula");
        this.dataExpiracao = Objects.requireNonNull(dataExpiracao, "dataExpiracao não pode ser nula");
        this.dataResposta = dataResposta;
    }

    
    public static ConviteProcessoSeletivo criar(
            UUID vagaId,
            UUID recrutadorUsuarioId,
            UUID candidatoUsuarioId,
            String mensagem
    ) {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime expiracao = agora.plusDays(7);

        return new ConviteProcessoSeletivo(
                UUID.randomUUID(),
                vagaId,
                recrutadorUsuarioId,
                candidatoUsuarioId,
                mensagem,
                StatusConviteProcesso.PENDENTE,
                agora,
                expiracao,
                null
        );
    }

    
    public static ConviteProcessoSeletivo rehydrate(
            UUID id,
            UUID vagaId,
            UUID recrutadorUsuarioId,
            UUID candidatoUsuarioId,
            String mensagem,
            StatusConviteProcesso status,
            LocalDateTime dataEnvio,
            LocalDateTime dataExpiracao,
            LocalDateTime dataResposta
    ) {
        return new ConviteProcessoSeletivo(
                id,
                vagaId,
                recrutadorUsuarioId,
                candidatoUsuarioId,
                mensagem,
                status,
                dataEnvio,
                dataExpiracao,
                dataResposta
        );
    }

    
    public ConviteProcessoSeletivo aceitar() {
        if (this.status != StatusConviteProcesso.PENDENTE) {
            throw new IllegalStateException("Apenas convites pendentes podem ser aceitos");
        }

        if (LocalDateTime.now().isAfter(this.dataExpiracao)) {
            throw new IllegalStateException("Este convite expirou");
        }

        return new ConviteProcessoSeletivo(
                this.id,
                this.vagaId,
                this.recrutadorUsuarioId,
                this.candidatoUsuarioId,
                this.mensagem,
                StatusConviteProcesso.ACEITO,
                this.dataEnvio,
                this.dataExpiracao,
                LocalDateTime.now()
        );
    }

    
    public ConviteProcessoSeletivo recusar() {
        if (this.status != StatusConviteProcesso.PENDENTE) {
            throw new IllegalStateException("Apenas convites pendentes podem ser recusados");
        }

        return new ConviteProcessoSeletivo(
                this.id,
                this.vagaId,
                this.recrutadorUsuarioId,
                this.candidatoUsuarioId,
                this.mensagem,
                StatusConviteProcesso.RECUSADO,
                this.dataEnvio,
                this.dataExpiracao,
                LocalDateTime.now()
        );
    }

    
    public ConviteProcessoSeletivo expirar() {
        return new ConviteProcessoSeletivo(
                this.id,
                this.vagaId,
                this.recrutadorUsuarioId,
                this.candidatoUsuarioId,
                this.mensagem,
                StatusConviteProcesso.EXPIRADO,
                this.dataEnvio,
                this.dataExpiracao,
                LocalDateTime.now()
        );
    }

    
    public boolean estaExpirado() {
        return LocalDateTime.now().isAfter(dataExpiracao) &&
               status == StatusConviteProcesso.PENDENTE;
    }

    public UUID getId() {
        return id;
    }

    public UUID getVagaId() {
        return vagaId;
    }

    public UUID getRecrutadorUsuarioId() {
        return recrutadorUsuarioId;
    }

    public UUID getCandidatoUsuarioId() {
        return candidatoUsuarioId;
    }

    public String getMensagem() {
        return mensagem;
    }

    public StatusConviteProcesso getStatus() {
        return status;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public LocalDateTime getDataResposta() {
        return dataResposta;
    }
}
