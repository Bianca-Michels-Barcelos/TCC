package com.barcelos.recrutamento.core.model;

import com.barcelos.recrutamento.core.model.vo.EnderecoSimples;
import com.barcelos.recrutamento.core.model.vo.Sigla;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class VagaTest {

    @Test
    void deveCriarNovaVagaComAtributosPadrao() {
        UUID organizacaoId = UUID.randomUUID();
        UUID recrutadorId = UUID.randomUUID();
        UUID nivelExperienciaId = UUID.randomUUID();
        EnderecoSimples endereco = new EnderecoSimples("São Paulo", new Sigla("SP"));
        BigDecimal salario = new BigDecimal("5000.00");
        LocalDate dataPublicacao = LocalDate.now();

        Vaga vaga = Vaga.nova(organizacaoId, recrutadorId, "Desenvolvedor Java", "Descrição da vaga",
                "Requisitos necessários", salario, dataPublicacao, StatusVaga.ABERTA,
                TipoContrato.CLT, ModalidadeTrabalho.REMOTO, "8h-17h", nivelExperienciaId, endereco);

        assertThat(vaga).isNotNull();
        assertThat(vaga.getId()).isNotNull();
        assertThat(vaga.getOrganizacaoId()).isEqualTo(organizacaoId);
        assertThat(vaga.getRecrutadorUsuarioId()).isEqualTo(recrutadorId);
        assertThat(vaga.getTitulo()).isEqualTo("Desenvolvedor Java");
        assertThat(vaga.getDescricao()).isEqualTo("Descrição da vaga");
        assertThat(vaga.getRequisitos()).isEqualTo("Requisitos necessários");
        assertThat(vaga.getSalario()).isEqualTo(salario);
        assertThat(vaga.getDataPublicacao()).isEqualTo(dataPublicacao);
        assertThat(vaga.getStatus()).isEqualTo(StatusVaga.ABERTA);
        assertThat(vaga.getTipoContrato()).isEqualTo(TipoContrato.CLT);
        assertThat(vaga.getModalidade()).isEqualTo(ModalidadeTrabalho.REMOTO);
        assertThat(vaga.getHorarioTrabalho()).isEqualTo("8h-17h");
        assertThat(vaga.getNivelExperienciaId()).isEqualTo(nivelExperienciaId);
        assertThat(vaga.getEndereco()).isEqualTo(endereco);
        assertThat(vaga.isAtivo()).isTrue();
        assertThat(vaga.getMotivoCancelamento()).isNull();
    }

    @Test
    void deveRehydratarVagaExistente() {
        UUID id = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        UUID recrutadorId = UUID.randomUUID();
        UUID nivelExperienciaId = UUID.randomUUID();
        EnderecoSimples endereco = new EnderecoSimples("Rio de Janeiro", new Sigla("RJ"));
        BigDecimal salario = new BigDecimal("8000.00");
        LocalDate dataPublicacao = LocalDate.of(2025, 1, 15);

        Vaga vaga = Vaga.rehydrate(id, organizacaoId, recrutadorId, "Analista de Dados", "Descrição",
                "Requisitos", salario, dataPublicacao, StatusVaga.FECHADA,
                TipoContrato.PJ, ModalidadeTrabalho.HIBRIDO, "Flexível", nivelExperienciaId, endereco,
                false, "Vaga preenchida");

        assertThat(vaga.getId()).isEqualTo(id);
        assertThat(vaga.getStatus()).isEqualTo(StatusVaga.FECHADA);
        assertThat(vaga.isAtivo()).isFalse();
        assertThat(vaga.getMotivoCancelamento()).isEqualTo("Vaga preenchida");
    }

    @Test
    void deveAtualizarTituloMantendoImutabilidade() {
        Vaga original = criarVagaPadrao();
        
        Vaga atualizada = original.comTitulo("Novo Título");

        assertThat(original.getTitulo()).isEqualTo("Desenvolvedor Java");
        assertThat(atualizada.getTitulo()).isEqualTo("Novo Título");
        assertThat(atualizada.getId()).isEqualTo(original.getId());
    }

    @Test
    void deveAtualizarDescricaoMantendoImutabilidade() {
        Vaga original = criarVagaPadrao();
        
        Vaga atualizada = original.comDescricao("Nova descrição");

        assertThat(original.getDescricao()).isEqualTo("Descrição da vaga");
        assertThat(atualizada.getDescricao()).isEqualTo("Nova descrição");
        assertThat(atualizada.getId()).isEqualTo(original.getId());
    }

    @Test
    void deveAtualizarRequisitosMantendoImutabilidade() {
        Vaga original = criarVagaPadrao();
        
        Vaga atualizada = original.comRequisitos("Novos requisitos");

        assertThat(original.getRequisitos()).isEqualTo("Requisitos necessários");
        assertThat(atualizada.getRequisitos()).isEqualTo("Novos requisitos");
        assertThat(atualizada.getId()).isEqualTo(original.getId());
    }

    @Test
    void deveAtualizarSalarioMantendoImutabilidade() {
        Vaga original = criarVagaPadrao();
        BigDecimal novoSalario = new BigDecimal("10000.00");
        
        Vaga atualizada = original.comSalario(novoSalario);

        assertThat(original.getSalario()).isEqualTo(new BigDecimal("5000.00"));
        assertThat(atualizada.getSalario()).isEqualTo(novoSalario);
    }

    @Test
    void deveAtualizarStatusMantendoImutabilidade() {
        Vaga original = criarVagaPadrao();
        
        Vaga atualizada = original.comStatus(StatusVaga.FECHADA);

        assertThat(original.getStatus()).isEqualTo(StatusVaga.ABERTA);
        assertThat(atualizada.getStatus()).isEqualTo(StatusVaga.FECHADA);
    }

    @Test
    void deveAtualizarTipoContratoMantendoImutabilidade() {
        Vaga original = criarVagaPadrao();
        
        Vaga atualizada = original.comTipoContrato(TipoContrato.PJ);

        assertThat(original.getTipoContrato()).isEqualTo(TipoContrato.CLT);
        assertThat(atualizada.getTipoContrato()).isEqualTo(TipoContrato.PJ);
    }

    @Test
    void deveAtualizarModalidadeMantendoImutabilidade() {
        Vaga original = criarVagaPadrao();
        
        Vaga atualizada = original.comModalidade(ModalidadeTrabalho.PRESENCIAL);

        assertThat(original.getModalidade()).isEqualTo(ModalidadeTrabalho.REMOTO);
        assertThat(atualizada.getModalidade()).isEqualTo(ModalidadeTrabalho.PRESENCIAL);
    }

    @Test
    void deveAtualizarHorarioTrabalhoMantendoImutabilidade() {
        Vaga original = criarVagaPadrao();
        
        Vaga atualizada = original.comHorarioTrabalho("9h-18h");

        assertThat(original.getHorarioTrabalho()).isEqualTo("8h-17h");
        assertThat(atualizada.getHorarioTrabalho()).isEqualTo("9h-18h");
    }

    @Test
    void deveAtualizarNivelExperienciaMantendoImutabilidade() {
        Vaga original = criarVagaPadrao();
        UUID novoNivelId = UUID.randomUUID();
        
        Vaga atualizada = original.comNivelExperienciaId(novoNivelId);

        assertThat(atualizada.getNivelExperienciaId()).isEqualTo(novoNivelId);
        assertThat(atualizada.getId()).isEqualTo(original.getId());
    }

    @Test
    void deveAtualizarEnderecoMantendoImutabilidade() {
        Vaga original = criarVagaPadrao();
        EnderecoSimples novoEndereco = new EnderecoSimples("Brasília", new Sigla("DF"));
        
        Vaga atualizada = original.comEndereco(novoEndereco);

        assertThat(atualizada.getEndereco()).isEqualTo(novoEndereco);
        assertThat(original.getEndereco().cidade()).isEqualTo("São Paulo");
    }

    @Test
    void deveAtualizarRecrutadorMantendoImutabilidade() {
        Vaga original = criarVagaPadrao();
        UUID novoRecrutadorId = UUID.randomUUID();
        
        Vaga atualizada = original.comRecrutador(novoRecrutadorId);

        assertThat(atualizada.getRecrutadorUsuarioId()).isEqualTo(novoRecrutadorId);
        assertThat(atualizada.getRecrutadorUsuarioId()).isNotEqualTo(original.getRecrutadorUsuarioId());
    }

    @Test
    void deveAtivarVaga() {
        Vaga vaga = criarVagaPadrao().desativar();
        
        Vaga ativada = vaga.ativar();

        assertThat(vaga.isAtivo()).isFalse();
        assertThat(ativada.isAtivo()).isTrue();
    }

    @Test
    void deveDesativarVaga() {
        Vaga vaga = criarVagaPadrao();
        
        Vaga desativada = vaga.desativar();

        assertThat(vaga.isAtivo()).isTrue();
        assertThat(desativada.isAtivo()).isFalse();
    }

    @Test
    void deveFecharVaga() {
        Vaga vaga = criarVagaPadrao();
        
        Vaga fechada = vaga.fechar();

        assertThat(vaga.getStatus()).isEqualTo(StatusVaga.ABERTA);
        assertThat(fechada.getStatus()).isEqualTo(StatusVaga.FECHADA);
    }

    @Test
    void deveCancelarVagaComMotivo() {
        Vaga vaga = criarVagaPadrao();
        String motivo = "Vaga não aprovada";
        
        Vaga cancelada = vaga.cancelar(motivo);

        assertThat(vaga.getStatus()).isEqualTo(StatusVaga.ABERTA);
        assertThat(vaga.getMotivoCancelamento()).isNull();
        assertThat(cancelada.getStatus()).isEqualTo(StatusVaga.CANCELADA);
        assertThat(cancelada.getMotivoCancelamento()).isEqualTo(motivo);
    }

    @Test
    void naoDeveCancelarVagaSemMotivo() {
        Vaga vaga = criarVagaPadrao();

        assertThatThrownBy(() -> vaga.cancelar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Motivo do cancelamento não pode ser nulo ou vazio");
    }

    @Test
    void naoDeveCancelarVagaComMotivoVazio() {
        Vaga vaga = criarVagaPadrao();

        assertThatThrownBy(() -> vaga.cancelar("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Motivo do cancelamento não pode ser nulo ou vazio");
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID organizacaoId = UUID.randomUUID();
        UUID recrutadorId = UUID.randomUUID();
        UUID nivelExperienciaId = UUID.randomUUID();
        EnderecoSimples endereco = new EnderecoSimples("São Paulo", new Sigla("SP"));
        BigDecimal salario = new BigDecimal("5000.00");
        LocalDate dataPublicacao = LocalDate.now();

        assertThatThrownBy(() -> Vaga.nova(null, recrutadorId, "Título", "Descrição",
                "Requisitos", salario, dataPublicacao, StatusVaga.ABERTA,
                TipoContrato.CLT, ModalidadeTrabalho.REMOTO, "8h-17h", nivelExperienciaId, endereco))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("OrganizacaoId não pode ser nulo");

        assertThatThrownBy(() -> Vaga.nova(organizacaoId, null, "Título", "Descrição",
                "Requisitos", salario, dataPublicacao, StatusVaga.ABERTA,
                TipoContrato.CLT, ModalidadeTrabalho.REMOTO, "8h-17h", nivelExperienciaId, endereco))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("RecrutadorUsuarioId não pode ser nulo");

        assertThatThrownBy(() -> Vaga.nova(organizacaoId, recrutadorId, null, "Descrição",
                "Requisitos", salario, dataPublicacao, StatusVaga.ABERTA,
                TipoContrato.CLT, ModalidadeTrabalho.REMOTO, "8h-17h", nivelExperienciaId, endereco))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Titulo não pode ser nulo");

        assertThatThrownBy(() -> Vaga.nova(organizacaoId, recrutadorId, "Título", null,
                "Requisitos", salario, dataPublicacao, StatusVaga.ABERTA,
                TipoContrato.CLT, ModalidadeTrabalho.REMOTO, "8h-17h", nivelExperienciaId, endereco))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Descricao não pode ser nula");

        assertThatThrownBy(() -> Vaga.nova(organizacaoId, recrutadorId, "Título", "Descrição",
                null, salario, dataPublicacao, StatusVaga.ABERTA,
                TipoContrato.CLT, ModalidadeTrabalho.REMOTO, "8h-17h", nivelExperienciaId, endereco))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Requisitos não pode ser nulo");

        assertThatThrownBy(() -> Vaga.nova(organizacaoId, recrutadorId, "Título", "Descrição",
                "Requisitos", salario, null, StatusVaga.ABERTA,
                TipoContrato.CLT, ModalidadeTrabalho.REMOTO, "8h-17h", nivelExperienciaId, endereco))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("DataPublicacao não pode ser nula");

        assertThatThrownBy(() -> Vaga.nova(organizacaoId, recrutadorId, "Título", "Descrição",
                "Requisitos", salario, dataPublicacao, null,
                TipoContrato.CLT, ModalidadeTrabalho.REMOTO, "8h-17h", nivelExperienciaId, endereco))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Status não pode ser nulo");

        assertThatThrownBy(() -> Vaga.nova(organizacaoId, recrutadorId, "Título", "Descrição",
                "Requisitos", salario, dataPublicacao, StatusVaga.ABERTA,
                null, ModalidadeTrabalho.REMOTO, "8h-17h", nivelExperienciaId, endereco))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("TipoContrato não pode ser nulo");

        assertThatThrownBy(() -> Vaga.nova(organizacaoId, recrutadorId, "Título", "Descrição",
                "Requisitos", salario, dataPublicacao, StatusVaga.ABERTA,
                TipoContrato.CLT, null, "8h-17h", nivelExperienciaId, endereco))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Modalidade não pode ser nula");

        assertThatThrownBy(() -> Vaga.nova(organizacaoId, recrutadorId, "Título", "Descrição",
                "Requisitos", salario, dataPublicacao, StatusVaga.ABERTA,
                TipoContrato.CLT, ModalidadeTrabalho.REMOTO, null, nivelExperienciaId, endereco))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("HorarioTrabalho não pode ser nulo");
    }

    @Test
    void deveValidarRecrutadorNaoNuloAoAtualizar() {
        Vaga vaga = criarVagaPadrao();

        assertThatThrownBy(() -> vaga.comRecrutador(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("RecrutadorUsuarioId não pode ser nulo");
    }

    private Vaga criarVagaPadrao() {
        UUID organizacaoId = UUID.randomUUID();
        UUID recrutadorId = UUID.randomUUID();
        UUID nivelExperienciaId = UUID.randomUUID();
        EnderecoSimples endereco = new EnderecoSimples("São Paulo", new Sigla("SP"));
        BigDecimal salario = new BigDecimal("5000.00");
        LocalDate dataPublicacao = LocalDate.now();

        return Vaga.nova(organizacaoId, recrutadorId, "Desenvolvedor Java", "Descrição da vaga",
                "Requisitos necessários", salario, dataPublicacao, StatusVaga.ABERTA,
                TipoContrato.CLT, ModalidadeTrabalho.REMOTO, "8h-17h", nivelExperienciaId, endereco);
    }
}

