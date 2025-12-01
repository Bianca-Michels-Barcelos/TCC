package com.barcelos.recrutamento.data.entity;

import com.barcelos.recrutamento.core.model.StatusConvite;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "convites_recrutador",
        indexes = {
                @Index(name = "idx_convite_token", columnList = "token"),
                @Index(name = "idx_convite_email", columnList = "email"),
                @Index(name = "idx_convite_organizacao", columnList = "organizacao_id")
        }
)
public class ConviteRecrutadorEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "organizacao_id", nullable = false)
    private UUID organizacaoId;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusConvite status;

    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(name = "data_aceite")
    private LocalDateTime dataAceite;

    protected ConviteRecrutadorEntity() {
    }

    public ConviteRecrutadorEntity(UUID id, UUID organizacaoId, String email, String token,
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(UUID organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public StatusConvite getStatus() {
        return status;
    }

    public void setStatus(StatusConvite status) {
        this.status = status;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public void setDataExpiracao(LocalDateTime dataExpiracao) {
        this.dataExpiracao = dataExpiracao;
    }

    public LocalDateTime getDataAceite() {
        return dataAceite;
    }

    public void setDataAceite(LocalDateTime dataAceite) {
        this.dataAceite = dataAceite;
    }
}
