package com.barcelos.recrutamento.core.model;

import com.barcelos.recrutamento.core.model.vo.EnderecoSimples;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Vaga {
    private final UUID id;
    private final UUID organizacaoId;
    private final UUID recrutadorUsuarioId;
    private final String titulo;
    private final String descricao;
    private final String requisitos;
    private final BigDecimal salario;
    private final LocalDate dataPublicacao;
    private final StatusVaga status;
    private final TipoContrato tipoContrato;
    private final ModalidadeTrabalho modalidade;
    private final String horarioTrabalho;
    private final UUID nivelExperienciaId;
    private final EnderecoSimples endereco;
    private final boolean ativo;
    private final String motivoCancelamento;

    
    private Vaga(UUID id, UUID organizacaoId, UUID recrutadorUsuarioId, String titulo, String descricao,
                String requisitos, BigDecimal salario, LocalDate dataPublicacao, StatusVaga status,
                TipoContrato tipoContrato, ModalidadeTrabalho modalidade, String horarioTrabalho,
                UUID nivelExperienciaId, EnderecoSimples endereco, boolean ativo, String motivoCancelamento) {

        this.id = Objects.requireNonNull(id, "ID não pode ser nulo");
        this.organizacaoId = Objects.requireNonNull(organizacaoId, "OrganizacaoId não pode ser nulo");
        this.recrutadorUsuarioId = Objects.requireNonNull(recrutadorUsuarioId, "RecrutadorUsuarioId não pode ser nulo");
        this.titulo = Objects.requireNonNull(titulo, "Titulo não pode ser nulo");
        this.descricao = Objects.requireNonNull(descricao, "Descricao não pode ser nula");
        this.requisitos = Objects.requireNonNull(requisitos, "Requisitos não pode ser nulo");
        this.salario = salario;
        this.dataPublicacao = Objects.requireNonNull(dataPublicacao, "DataPublicacao não pode ser nula");
        this.status = Objects.requireNonNull(status, "Status não pode ser nulo");
        this.tipoContrato = Objects.requireNonNull(tipoContrato, "TipoContrato não pode ser nulo");
        this.modalidade = Objects.requireNonNull(modalidade, "Modalidade não pode ser nula");
        this.horarioTrabalho = Objects.requireNonNull(horarioTrabalho, "HorarioTrabalho não pode ser nulo");
        this.nivelExperienciaId = nivelExperienciaId;
        this.endereco = endereco;
        this.ativo = ativo;
        this.motivoCancelamento = motivoCancelamento;
    }

    
    public static Vaga nova(UUID organizacaoId, UUID recrutadorUsuarioId, String titulo, String descricao,
                            String requisitos, BigDecimal salario, LocalDate dataPublicacao, StatusVaga status,
                            TipoContrato tipoContrato, ModalidadeTrabalho modalidade, String horarioTrabalho,
                            UUID nivelExperienciaId, EnderecoSimples endereco) {
        return new Vaga(UUID.randomUUID(), organizacaoId, recrutadorUsuarioId, titulo, descricao,
                requisitos, salario, dataPublicacao, status, tipoContrato, modalidade, horarioTrabalho,
                nivelExperienciaId, endereco, true, null);
    }

    
    public static Vaga rehydrate(UUID id, UUID organizacaoId, UUID recrutadorUsuarioId, String titulo, String descricao,
                                 String requisitos, BigDecimal salario, LocalDate dataPublicacao, StatusVaga status,
                                 TipoContrato tipoContrato, ModalidadeTrabalho modalidade, String horarioTrabalho,
                                 UUID nivelExperienciaId, EnderecoSimples endereco, boolean ativo, String motivoCancelamento) {
        return new Vaga(id, organizacaoId, recrutadorUsuarioId, titulo, descricao, requisitos, salario,
                dataPublicacao, status, tipoContrato, modalidade, horarioTrabalho, nivelExperienciaId, endereco,
                ativo, motivoCancelamento);
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public UUID getRecrutadorUsuarioId() {
        return recrutadorUsuarioId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getRequisitos() {
        return requisitos;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public LocalDate getDataPublicacao() {
        return dataPublicacao;
    }

    public StatusVaga getStatus() {
        return status;
    }

    public TipoContrato getTipoContrato() {
        return tipoContrato;
    }

    public ModalidadeTrabalho getModalidade() {
        return modalidade;
    }

    public String getHorarioTrabalho() {
        return horarioTrabalho;
    }

    public UUID getNivelExperienciaId() {
        return nivelExperienciaId;
    }

    public EnderecoSimples getEndereco() {
        return endereco;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    
    public Vaga comTitulo(String novoTitulo) {
        return new Vaga(id, organizacaoId, recrutadorUsuarioId, novoTitulo, descricao, requisitos,
                salario, dataPublicacao, status, tipoContrato, modalidade, horarioTrabalho,
                nivelExperienciaId, endereco, ativo, motivoCancelamento);
    }

    
    public Vaga comDescricao(String novaDescricao) {
        return new Vaga(id, organizacaoId, recrutadorUsuarioId, titulo, novaDescricao, requisitos,
                salario, dataPublicacao, status, tipoContrato, modalidade, horarioTrabalho,
                nivelExperienciaId, endereco, ativo, motivoCancelamento);
    }

    
    public Vaga comRequisitos(String novosRequisitos) {
        return new Vaga(id, organizacaoId, recrutadorUsuarioId, titulo, descricao, novosRequisitos,
                salario, dataPublicacao, status, tipoContrato, modalidade, horarioTrabalho,
                nivelExperienciaId, endereco, ativo, motivoCancelamento);
    }

    
    public Vaga comSalario(BigDecimal novoSalario) {
        return new Vaga(id, organizacaoId, recrutadorUsuarioId, titulo, descricao, requisitos,
                novoSalario, dataPublicacao, status, tipoContrato, modalidade, horarioTrabalho,
                nivelExperienciaId, endereco, ativo, motivoCancelamento);
    }

    
    public Vaga comStatus(StatusVaga novoStatus) {
        return new Vaga(id, organizacaoId, recrutadorUsuarioId, titulo, descricao, requisitos,
                salario, dataPublicacao, novoStatus, tipoContrato, modalidade, horarioTrabalho,
                nivelExperienciaId, endereco, ativo, motivoCancelamento);
    }

    
    public Vaga comTipoContrato(TipoContrato novoTipoContrato) {
        return new Vaga(id, organizacaoId, recrutadorUsuarioId, titulo, descricao, requisitos,
                salario, dataPublicacao, status, novoTipoContrato, modalidade, horarioTrabalho,
                nivelExperienciaId, endereco, ativo, motivoCancelamento);
    }

    
    public Vaga comModalidade(ModalidadeTrabalho novaModalidade) {
        return new Vaga(id, organizacaoId, recrutadorUsuarioId, titulo, descricao, requisitos,
                salario, dataPublicacao, status, tipoContrato, novaModalidade, horarioTrabalho,
                nivelExperienciaId, endereco, ativo, motivoCancelamento);
    }

    
    public Vaga comHorarioTrabalho(String novoHorarioTrabalho) {
        return new Vaga(id, organizacaoId, recrutadorUsuarioId, titulo, descricao, requisitos,
                salario, dataPublicacao, status, tipoContrato, modalidade, novoHorarioTrabalho,
                nivelExperienciaId, endereco, ativo, motivoCancelamento);
    }

    
    public Vaga comNivelExperienciaId(UUID novoNivelExperienciaId) {
        return new Vaga(id, organizacaoId, recrutadorUsuarioId, titulo, descricao, requisitos,
                salario, dataPublicacao, status, tipoContrato, modalidade, horarioTrabalho,
                novoNivelExperienciaId, endereco, ativo, motivoCancelamento);
    }

    
    public Vaga comEndereco(EnderecoSimples novoEndereco) {
        return new Vaga(id, organizacaoId, recrutadorUsuarioId, titulo, descricao, requisitos,
                salario, dataPublicacao, status, tipoContrato, modalidade, horarioTrabalho,
                nivelExperienciaId, novoEndereco, ativo, motivoCancelamento);
    }

    
    public Vaga comRecrutador(UUID novoRecrutadorUsuarioId) {
        return new Vaga(id, organizacaoId, Objects.requireNonNull(novoRecrutadorUsuarioId, "RecrutadorUsuarioId não pode ser nulo"),
                titulo, descricao, requisitos, salario, dataPublicacao, status, tipoContrato, modalidade,
                horarioTrabalho, nivelExperienciaId, endereco, ativo, motivoCancelamento);
    }

    
    public Vaga ativar() {
        return new Vaga(id, organizacaoId, recrutadorUsuarioId, titulo, descricao, requisitos,
                salario, dataPublicacao, status, tipoContrato, modalidade, horarioTrabalho,
                nivelExperienciaId, endereco, true, motivoCancelamento);
    }

    
    public Vaga desativar() {
        return new Vaga(id, organizacaoId, recrutadorUsuarioId, titulo, descricao, requisitos,
                salario, dataPublicacao, status, tipoContrato, modalidade, horarioTrabalho,
                nivelExperienciaId, endereco, false, motivoCancelamento);
    }

    
    public Vaga fechar() {
        return new Vaga(id, organizacaoId, recrutadorUsuarioId, titulo, descricao, requisitos,
                salario, dataPublicacao, StatusVaga.FECHADA, tipoContrato, modalidade, horarioTrabalho,
                nivelExperienciaId, endereco, ativo, motivoCancelamento);
    }

    
    public Vaga cancelar(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Motivo do cancelamento não pode ser nulo ou vazio");
        }
        return new Vaga(id, organizacaoId, recrutadorUsuarioId, titulo, descricao, requisitos,
                salario, dataPublicacao, StatusVaga.CANCELADA, tipoContrato, modalidade, horarioTrabalho,
                nivelExperienciaId, endereco, ativo, motivo);
    }
}