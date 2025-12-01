package com.barcelos.recrutamento.core.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ResetSenha {

    private final UUID id;
    private final UUID usuarioId;
    private final String token;
    private final StatusResetSenha status;
    private final LocalDateTime dataSolicitacao;
    private final LocalDateTime dataExpiracao;
    private final LocalDateTime dataUso;

    private ResetSenha(UUID id, UUID usuarioId, String token,
                       StatusResetSenha status, LocalDateTime dataSolicitacao,
                       LocalDateTime dataExpiracao, LocalDateTime dataUso) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.token = token;
        this.status = status;
        this.dataSolicitacao = dataSolicitacao;
        this.dataExpiracao = dataExpiracao;
        this.dataUso = dataUso;
    }

    
    public static ResetSenha criar(UUID usuarioId, String token) {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime expiracao = agora.plusHours(24);

        return new ResetSenha(
                UUID.randomUUID(),
                usuarioId,
                token,
                StatusResetSenha.PENDENTE,
                agora,
                expiracao,
                null
        );
    }

    
    public static ResetSenha reconstruir(UUID id, UUID usuarioId, String token,
                                         StatusResetSenha status, LocalDateTime dataSolicitacao,
                                         LocalDateTime dataExpiracao, LocalDateTime dataUso) {
        return new ResetSenha(id, usuarioId, token, status,
                dataSolicitacao, dataExpiracao, dataUso);
    }

    
    public ResetSenha estenderValidade() {
        if (status != StatusResetSenha.PENDENTE) {
            throw new IllegalStateException("Apenas tokens pendentes podem ter a validade estendida");
        }

        LocalDateTime novaExpiracao = LocalDateTime.now().plusHours(24);

        return new ResetSenha(
                this.id,
                this.usuarioId,
                this.token,
                this.status,
                this.dataSolicitacao,
                novaExpiracao,
                this.dataUso
        );
    }

    
    public ResetSenha marcarComoUsado() {
        if (status != StatusResetSenha.PENDENTE) {
            throw new IllegalStateException("Token não está pendente");
        }

        if (LocalDateTime.now().isAfter(dataExpiracao)) {
            throw new IllegalStateException("Token expirado");
        }

        return new ResetSenha(
                this.id,
                this.usuarioId,
                this.token,
                StatusResetSenha.USADO,
                this.dataSolicitacao,
                this.dataExpiracao,
                LocalDateTime.now()
        );
    }

    
    public ResetSenha marcarComoExpirado() {
        return new ResetSenha(
                this.id,
                this.usuarioId,
                this.token,
                StatusResetSenha.EXPIRADO,
                this.dataSolicitacao,
                this.dataExpiracao,
                this.dataUso
        );
    }

    
    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(dataExpiracao);
    }

    
    public boolean isValido() {
        return status == StatusResetSenha.PENDENTE && !isExpirado();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getToken() {
        return token;
    }

    public StatusResetSenha getStatus() {
        return status;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public LocalDateTime getDataUso() {
        return dataUso;
    }
}

