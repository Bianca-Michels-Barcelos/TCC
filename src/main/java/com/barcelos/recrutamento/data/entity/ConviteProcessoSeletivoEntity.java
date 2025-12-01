package com.barcelos.recrutamento.data.entity;

import com.barcelos.recrutamento.core.model.StatusConviteProcesso;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "convites_processo_seletivo")
public class ConviteProcessoSeletivoEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_id", nullable = false)
    private VagaEntity vaga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recrutador_usuario_id", nullable = false)
    private UsuarioEntity recrutador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidato_usuario_id", nullable = false)
    private UsuarioEntity candidato;

    @Column(name = "mensagem", columnDefinition = "TEXT")
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusConviteProcesso status;

    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(name = "data_resposta")
    private LocalDateTime dataResposta;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public VagaEntity getVaga() {
        return vaga;
    }

    public void setVaga(VagaEntity vaga) {
        this.vaga = vaga;
    }

    public UsuarioEntity getRecrutador() {
        return recrutador;
    }

    public void setRecrutador(UsuarioEntity recrutador) {
        this.recrutador = recrutador;
    }

    public UsuarioEntity getCandidato() {
        return candidato;
    }

    public void setCandidato(UsuarioEntity candidato) {
        this.candidato = candidato;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public StatusConviteProcesso getStatus() {
        return status;
    }

    public void setStatus(StatusConviteProcesso status) {
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

    public LocalDateTime getDataResposta() {
        return dataResposta;
    }

    public void setDataResposta(LocalDateTime dataResposta) {
        this.dataResposta = dataResposta;
    }
}
