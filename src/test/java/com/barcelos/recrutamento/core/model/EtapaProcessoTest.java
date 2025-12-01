package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class EtapaProcessoTest {

    @Test
    void deveCriarNovaEtapaProcessoComAtributosPadrao() {
        UUID vagaId = UUID.randomUUID();
        LocalDateTime dataInicio = LocalDateTime.of(2025, 2, 1, 9, 0);
        LocalDateTime dataFim = LocalDateTime.of(2025, 2, 15, 18, 0);

        EtapaProcesso etapa = EtapaProcesso.criar(vagaId, "Entrevista Técnica", 
                "Avaliação de conhecimentos técnicos", TipoEtapa.ENTREVISTA_PRESENCIAL, 
                1, dataInicio, dataFim);

        assertThat(etapa).isNotNull();
        assertThat(etapa.getId()).isNotNull();
        assertThat(etapa.getVagaId()).isEqualTo(vagaId);
        assertThat(etapa.getNome()).isEqualTo("Entrevista Técnica");
        assertThat(etapa.getDescricao()).isEqualTo("Avaliação de conhecimentos técnicos");
        assertThat(etapa.getTipo()).isEqualTo(TipoEtapa.ENTREVISTA_PRESENCIAL);
        assertThat(etapa.getOrdem()).isEqualTo(1);
        assertThat(etapa.getStatus()).isEqualTo(StatusEtapa.PENDENTE);
        assertThat(etapa.getDataInicio()).isEqualTo(dataInicio);
        assertThat(etapa.getDataFim()).isEqualTo(dataFim);
        assertThat(etapa.getDataCriacao()).isNotNull();
    }

    @Test
    void deveCriarEtapaSemDatasOpcionais() {
        UUID vagaId = UUID.randomUUID();

        EtapaProcesso etapa = EtapaProcesso.criar(vagaId, "Triagem", 
                "Triagem inicial", TipoEtapa.TRIAGEM_CURRICULO, 1, null, null);

        assertThat(etapa).isNotNull();
        assertThat(etapa.getDataInicio()).isNull();
        assertThat(etapa.getDataFim()).isNull();
    }

    @Test
    void deveRehydratarEtapaProcessoExistente() {
        UUID id = UUID.randomUUID();
        UUID vagaId = UUID.randomUUID();
        LocalDateTime dataInicio = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime dataFim = LocalDateTime.of(2025, 1, 10, 18, 0);
        LocalDateTime dataCriacao = LocalDateTime.of(2025, 1, 1, 9, 0);

        EtapaProcesso etapa = EtapaProcesso.rehydrate(id, vagaId, "Teste Técnico", 
                "Avaliação técnica", TipoEtapa.TESTE_TECNICO, 2, StatusEtapa.CONCLUIDA,
                dataInicio, dataFim, dataCriacao);

        assertThat(etapa.getId()).isEqualTo(id);
        assertThat(etapa.getVagaId()).isEqualTo(vagaId);
        assertThat(etapa.getNome()).isEqualTo("Teste Técnico");
        assertThat(etapa.getStatus()).isEqualTo(StatusEtapa.CONCLUIDA);
        assertThat(etapa.getOrdem()).isEqualTo(2);
    }

    @Test
    void deveIniciarEtapaPendente() {
        EtapaProcesso etapa = criarEtapaPadrao();
        
        EtapaProcesso iniciada = etapa.iniciar();

        assertThat(etapa.getStatus()).isEqualTo(StatusEtapa.PENDENTE);
        assertThat(iniciada.getStatus()).isEqualTo(StatusEtapa.EM_ANDAMENTO);
    }

    @Test
    void naoDeveIniciarEtapaJaIniciada() {
        EtapaProcesso etapa = criarEtapaPadrao().iniciar();

        assertThatThrownBy(() -> etapa.iniciar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Somente etapas pendentes podem ser iniciadas");
    }

    @Test
    void naoDeveIniciarEtapaConcluida() {
        EtapaProcesso etapa = criarEtapaPadrao().iniciar().concluir();

        assertThatThrownBy(() -> etapa.iniciar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Somente etapas pendentes podem ser iniciadas");
    }

    @Test
    void deveConcluirEtapaEmAndamento() {
        EtapaProcesso etapa = criarEtapaPadrao().iniciar();
        
        EtapaProcesso concluida = etapa.concluir();

        assertThat(etapa.getStatus()).isEqualTo(StatusEtapa.EM_ANDAMENTO);
        assertThat(concluida.getStatus()).isEqualTo(StatusEtapa.CONCLUIDA);
    }

    @Test
    void deveConcluirEtapaPendente() {
        EtapaProcesso etapa = criarEtapaPadrao();
        
        EtapaProcesso concluida = etapa.concluir();

        assertThat(etapa.getStatus()).isEqualTo(StatusEtapa.PENDENTE);
        assertThat(concluida.getStatus()).isEqualTo(StatusEtapa.CONCLUIDA);
    }

    @Test
    void naoDeveConcluirEtapaCancelada() {
        EtapaProcesso etapa = criarEtapaPadrao().cancelar();

        assertThatThrownBy(() -> etapa.concluir())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Somente etapas em andamento ou pendentes podem ser concluídas");
    }

    @Test
    void deveCancelarEtapaPendente() {
        EtapaProcesso etapa = criarEtapaPadrao();
        
        EtapaProcesso cancelada = etapa.cancelar();

        assertThat(etapa.getStatus()).isEqualTo(StatusEtapa.PENDENTE);
        assertThat(cancelada.getStatus()).isEqualTo(StatusEtapa.CANCELADA);
    }

    @Test
    void deveCancelarEtapaEmAndamento() {
        EtapaProcesso etapa = criarEtapaPadrao().iniciar();
        
        EtapaProcesso cancelada = etapa.cancelar();

        assertThat(etapa.getStatus()).isEqualTo(StatusEtapa.EM_ANDAMENTO);
        assertThat(cancelada.getStatus()).isEqualTo(StatusEtapa.CANCELADA);
    }

    @Test
    void naoDeveCancelarEtapaConcluida() {
        EtapaProcesso etapa = criarEtapaPadrao().concluir();

        assertThatThrownBy(() -> etapa.cancelar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Etapas concluídas não podem ser canceladas");
    }

    @Test
    void deveAtualizarEtapa() {
        EtapaProcesso etapa = criarEtapaPadrao();
        LocalDateTime novaDataInicio = LocalDateTime.of(2025, 3, 1, 10, 0);
        LocalDateTime novaDataFim = LocalDateTime.of(2025, 3, 15, 17, 0);
        
        EtapaProcesso atualizada = etapa.atualizar("Novo Nome", "Nova Descrição", 
                novaDataInicio, novaDataFim);

        assertThat(etapa.getNome()).isEqualTo("Entrevista Técnica");
        assertThat(atualizada.getNome()).isEqualTo("Novo Nome");
        assertThat(atualizada.getDescricao()).isEqualTo("Nova Descrição");
        assertThat(atualizada.getDataInicio()).isEqualTo(novaDataInicio);
        assertThat(atualizada.getDataFim()).isEqualTo(novaDataFim);
        assertThat(atualizada.getTipo()).isEqualTo(etapa.getTipo());
        assertThat(atualizada.getOrdem()).isEqualTo(etapa.getOrdem());
    }

    @Test
    void deveValidarCamposObrigatoriosAoCriar() {
        UUID vagaId = UUID.randomUUID();
        LocalDateTime dataInicio = LocalDateTime.now();
        LocalDateTime dataFim = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> EtapaProcesso.criar(null, "Nome", "Desc", 
                TipoEtapa.ENTREVISTA_PRESENCIAL, 1, dataInicio, dataFim))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID da vaga é obrigatório");

        assertThatThrownBy(() -> EtapaProcesso.criar(vagaId, null, "Desc", 
                TipoEtapa.ENTREVISTA_PRESENCIAL, 1, dataInicio, dataFim))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome da etapa é obrigatório");

        assertThatThrownBy(() -> EtapaProcesso.criar(vagaId, "   ", "Desc", 
                TipoEtapa.ENTREVISTA_PRESENCIAL, 1, dataInicio, dataFim))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome da etapa é obrigatório");

        assertThatThrownBy(() -> EtapaProcesso.criar(vagaId, "Nome", "Desc", 
                null, 1, dataInicio, dataFim))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tipo da etapa é obrigatório");
    }

    @Test
    void deveValidarOrdemMaiorQueZero() {
        UUID vagaId = UUID.randomUUID();

        assertThatThrownBy(() -> EtapaProcesso.criar(vagaId, "Nome", "Desc", 
                TipoEtapa.ENTREVISTA_PRESENCIAL, 0, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ordem deve ser maior que zero");

        assertThatThrownBy(() -> EtapaProcesso.criar(vagaId, "Nome", "Desc", 
                TipoEtapa.ENTREVISTA_PRESENCIAL, -1, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ordem deve ser maior que zero");
    }

    @Test
    void deveValidarDataFimPosteriorADataInicio() {
        UUID vagaId = UUID.randomUUID();
        LocalDateTime dataInicio = LocalDateTime.of(2025, 2, 10, 10, 0);
        LocalDateTime dataFim = LocalDateTime.of(2025, 2, 5, 10, 0);

        assertThatThrownBy(() -> EtapaProcesso.criar(vagaId, "Nome", "Desc", 
                TipoEtapa.ENTREVISTA_PRESENCIAL, 1, dataInicio, dataFim))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de fim não pode ser anterior à data de início");
    }

    @Test
    void deveValidarDataFimPosteriorADataInicioAoAtualizar() {
        EtapaProcesso etapa = criarEtapaPadrao();
        LocalDateTime dataInicio = LocalDateTime.of(2025, 2, 10, 10, 0);
        LocalDateTime dataFim = LocalDateTime.of(2025, 2, 5, 10, 0);

        assertThatThrownBy(() -> etapa.atualizar("Nome", "Desc", dataInicio, dataFim))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de fim não pode ser anterior à data de início");
    }

    @Test
    void deveValidarNomeObrigatorioAoAtualizar() {
        EtapaProcesso etapa = criarEtapaPadrao();

        assertThatThrownBy(() -> etapa.atualizar(null, "Desc", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome da etapa é obrigatório");

        assertThatThrownBy(() -> etapa.atualizar("   ", "Desc", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome da etapa é obrigatório");
    }

    private EtapaProcesso criarEtapaPadrao() {
        UUID vagaId = UUID.randomUUID();
        LocalDateTime dataInicio = LocalDateTime.of(2025, 2, 1, 9, 0);
        LocalDateTime dataFim = LocalDateTime.of(2025, 2, 15, 18, 0);

        return EtapaProcesso.criar(vagaId, "Entrevista Técnica", 
                "Avaliação de conhecimentos técnicos", TipoEtapa.ENTREVISTA_PRESENCIAL, 
                1, dataInicio, dataFim);
    }
}

