package com.barcelos.recrutamento.data.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reset_senha")
public class ResetSenhaEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(name = "data_uso")
    private LocalDateTime dataUso;

    public ResetSenhaEntity() {
    }

    public ResetSenhaEntity(UUID id, UUID usuarioId, String token, String status,
                            LocalDateTime dataSolicitacao, LocalDateTime dataExpiracao,
                            LocalDateTime dataUso) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.token = token;
        this.status = status;
        this.dataSolicitacao = dataSolicitacao;
        this.dataExpiracao = dataExpiracao;
        this.dataUso = dataUso;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public void setDataExpiracao(LocalDateTime dataExpiracao) {
        this.dataExpiracao = dataExpiracao;
    }

    public LocalDateTime getDataUso() {
        return dataUso;
    }

    public void setDataUso(LocalDateTime dataUso) {
        this.dataUso = dataUso;
    }
}

