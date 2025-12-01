package com.barcelos.recrutamento.core.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class EtapaProcesso {
    private final UUID id;
    private final UUID vagaId;
    private final String nome;
    private final String descricao;
    private final TipoEtapa tipo;
    private final int ordem;
    private final StatusEtapa status;
    private final LocalDateTime dataInicio;
    private final LocalDateTime dataFim;
    private final LocalDateTime dataCriacao;

    private EtapaProcesso(
            UUID id,
            UUID vagaId,
            String nome,
            String descricao,
            TipoEtapa tipo,
            int ordem,
            StatusEtapa status,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            LocalDateTime dataCriacao
    ) {
        this.id = id;
        this.vagaId = vagaId;
        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
        this.ordem = ordem;
        this.status = status;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.dataCriacao = dataCriacao;
    }

    
    public static EtapaProcesso criar(
            UUID vagaId,
            String nome,
            String descricao,
            TipoEtapa tipo,
            int ordem,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        if (vagaId == null) {
            throw new IllegalArgumentException("ID da vaga é obrigatório");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da etapa é obrigatório");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo da etapa é obrigatório");
        }
        if (ordem < 1) {
            throw new IllegalArgumentException("Ordem deve ser maior que zero");
        }
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data de fim não pode ser anterior à data de início");
        }

        return new EtapaProcesso(
                UUID.randomUUID(),
                vagaId,
                nome,
                descricao,
                tipo,
                ordem,
                StatusEtapa.PENDENTE,
                dataInicio,
                dataFim,
                LocalDateTime.now()
        );
    }

    
    public static EtapaProcesso rehydrate(
            UUID id,
            UUID vagaId,
            String nome,
            String descricao,
            TipoEtapa tipo,
            int ordem,
            StatusEtapa status,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            LocalDateTime dataCriacao
    ) {
        return new EtapaProcesso(
                id, vagaId, nome, descricao, tipo, ordem, status,
                dataInicio, dataFim, dataCriacao
        );
    }

    
    public EtapaProcesso iniciar() {
        if (status != StatusEtapa.PENDENTE) {
            throw new IllegalStateException("Somente etapas pendentes podem ser iniciadas");
        }
        return new EtapaProcesso(
                id, vagaId, nome, descricao, tipo, ordem,
                StatusEtapa.EM_ANDAMENTO, dataInicio, dataFim, dataCriacao
        );
    }

    
    public EtapaProcesso concluir() {
        if (status != StatusEtapa.EM_ANDAMENTO && status != StatusEtapa.PENDENTE) {
            throw new IllegalStateException("Somente etapas em andamento ou pendentes podem ser concluídas");
        }
        return new EtapaProcesso(
                id, vagaId, nome, descricao, tipo, ordem,
                StatusEtapa.CONCLUIDA, dataInicio, dataFim, dataCriacao
        );
    }

    
    public EtapaProcesso cancelar() {
        if (status == StatusEtapa.CONCLUIDA) {
            throw new IllegalStateException("Etapas concluídas não podem ser canceladas");
        }
        return new EtapaProcesso(
                id, vagaId, nome, descricao, tipo, ordem,
                StatusEtapa.CANCELADA, dataInicio, dataFim, dataCriacao
        );
    }

    
    public EtapaProcesso atualizar(
            String nome,
            String descricao,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da etapa é obrigatório");
        }
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data de fim não pode ser anterior à data de início");
        }

        return new EtapaProcesso(
                id, vagaId, nome, descricao, tipo, ordem, status,
                dataInicio, dataFim, dataCriacao
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getVagaId() {
        return vagaId;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public TipoEtapa getTipo() {
        return tipo;
    }

    public int getOrdem() {
        return ordem;
    }

    public StatusEtapa getStatus() {
        return status;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
}
