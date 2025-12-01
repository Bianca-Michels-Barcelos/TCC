package com.barcelos.recrutamento.core.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ConviteRecrutador {

    private final UUID id;
    private final UUID organizacaoId;
    private final String email;
    private final String token;
    private final StatusConvite status;
    private final LocalDateTime dataEnvio;
    private final LocalDateTime dataExpiracao;
    private final LocalDateTime dataAceite;

    private ConviteRecrutador(UUID id, UUID organizacaoId, String email, String token,
                             StatusConvite status, LocalDateTime dataEnvio,
                             LocalDateTime dataExpiracao, LocalDateTime dataAceite) {
        this.id = id;
        this.organizacaoId = organizacaoId;
        this.email = email;
        this.token = token;
        this.status = status;
        this.dataEnvio = dataEnvio;
        this.dataExpiracao = dataExpiracao;
        this.dataAceite = dataAceite;
    }

    
    public static ConviteRecrutador criar(UUID organizacaoId, String email, String token) {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime expiracao = agora.plusDays(7);

        return new ConviteRecrutador(
                UUID.randomUUID(),
                organizacaoId,
                email,
                token,
                StatusConvite.PENDENTE,
                agora,
                expiracao,
                null
        );
    }

    
    public static ConviteRecrutador reconstruir(UUID id, UUID organizacaoId, String email,
                                               String token, StatusConvite status,
                                               LocalDateTime dataEnvio, LocalDateTime dataExpiracao,
                                               LocalDateTime dataAceite) {
        return new ConviteRecrutador(id, organizacaoId, email, token, status,
                dataEnvio, dataExpiracao, dataAceite);
    }

    
    public ConviteRecrutador aceitar() {
        if (status != StatusConvite.PENDENTE) {
            throw new IllegalStateException("Convite não está pendente");
        }

        if (LocalDateTime.now().isAfter(dataExpiracao)) {
            throw new IllegalStateException("Convite expirado");
        }

        return new ConviteRecrutador(
                this.id,
                this.organizacaoId,
                this.email,
                this.token,
                StatusConvite.ACEITO,
                this.dataEnvio,
                this.dataExpiracao,
                LocalDateTime.now()
        );
    }

    
    public ConviteRecrutador recusar() {
        if (status != StatusConvite.PENDENTE) {
            throw new IllegalStateException("Convite não está pendente");
        }

        return new ConviteRecrutador(
                this.id,
                this.organizacaoId,
                this.email,
                this.token,
                StatusConvite.RECUSADO,
                this.dataEnvio,
                this.dataExpiracao,
                null
        );
    }

    
    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(dataExpiracao);
    }

    
    public boolean isValido() {
        return status == StatusConvite.PENDENTE && !isExpirado();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public StatusConvite getStatus() {
        return status;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public LocalDateTime getDataAceite() {
        return dataAceite;
    }
}
