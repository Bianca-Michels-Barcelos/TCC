package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ProcessoSeletivoTest {

    @Test
    void deveCriarNovoProcessoSeletivoComAtributosPadrao() {
        UUID candidaturaId = UUID.randomUUID();
        UUID etapaInicialId = UUID.randomUUID();

        ProcessoSeletivo processo = ProcessoSeletivo.novo(candidaturaId, etapaInicialId);

        assertThat(processo).isNotNull();
        assertThat(processo.getId()).isNotNull();
        assertThat(processo.getCandidaturaId()).isEqualTo(candidaturaId);
        assertThat(processo.getEtapaProcessoAtualId()).isEqualTo(etapaInicialId);
        assertThat(processo.getEtapaAtualId()).isEqualTo(etapaInicialId);
        assertThat(processo.getDataInicio()).isNotNull();
        assertThat(processo.getDataFim()).isNull();
        assertThat(processo.getDataUltimaMudanca()).isNotNull();
        assertThat(processo.isEmAndamento()).isTrue();
        assertThat(processo.isFinalizado()).isFalse();
    }

    @Test
    void deveRehydratarProcessoSeletivoExistente() {
        UUID id = UUID.randomUUID();
        UUID candidaturaId = UUID.randomUUID();
        UUID etapaId = UUID.randomUUID();
        LocalDateTime dataInicio = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime dataFim = LocalDateTime.of(2025, 1, 30, 15, 0);
        LocalDateTime dataUltimaMudanca = LocalDateTime.of(2025, 1, 30, 15, 0);

        ProcessoSeletivo processo = ProcessoSeletivo.rehydrate(id, candidaturaId, etapaId, 
                dataInicio, dataFim, dataUltimaMudanca);

        assertThat(processo.getId()).isEqualTo(id);
        assertThat(processo.getCandidaturaId()).isEqualTo(candidaturaId);
        assertThat(processo.getEtapaProcessoAtualId()).isEqualTo(etapaId);
        assertThat(processo.getDataInicio()).isEqualTo(dataInicio);
        assertThat(processo.getDataFim()).isEqualTo(dataFim);
        assertThat(processo.getDataUltimaMudanca()).isEqualTo(dataUltimaMudanca);
        assertThat(processo.isFinalizado()).isTrue();
        assertThat(processo.isEmAndamento()).isFalse();
    }

    @Test
    void deveAtualizarEtapaAtualMantendoImutabilidade() {
        ProcessoSeletivo original = criarProcessoPadrao();
        UUID novaEtapaId = UUID.randomUUID();
        
        ProcessoSeletivo atualizado = original.comEtapaAtual(novaEtapaId);

        assertThat(original.getEtapaProcessoAtualId()).isNotEqualTo(novaEtapaId);
        assertThat(atualizado.getEtapaProcessoAtualId()).isEqualTo(novaEtapaId);
        assertThat(atualizado.getDataUltimaMudanca()).isAfter(original.getDataUltimaMudanca());
    }

    @Test
    void naoDeveAtualizarEtapaAtualComEtapaNula() {
        ProcessoSeletivo processo = criarProcessoPadrao();

        assertThatThrownBy(() -> processo.comEtapaAtual(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nova etapa não pode ser nula");
    }

    @Test
    void deveAtualizarDataFimMantendoImutabilidade() {
        ProcessoSeletivo original = criarProcessoPadrao();
        LocalDateTime dataFim = LocalDateTime.now();
        
        ProcessoSeletivo atualizado = original.comDataFim(dataFim);

        assertThat(original.getDataFim()).isNull();
        assertThat(atualizado.getDataFim()).isEqualTo(dataFim);
    }

    @Test
    void deveFinalizarProcesso() {
        ProcessoSeletivo processo = criarProcessoPadrao();
        
        ProcessoSeletivo finalizado = processo.finalizar();

        assertThat(processo.getDataFim()).isNull();
        assertThat(processo.isEmAndamento()).isTrue();
        assertThat(finalizado.getDataFim()).isNotNull();
        assertThat(finalizado.isFinalizado()).isTrue();
        assertThat(finalizado.isEmAndamento()).isFalse();
    }

    @Test
    void naoDeveFinalizarProcessoJaFinalizado() {
        ProcessoSeletivo processo = criarProcessoPadrao().finalizar();

        assertThatThrownBy(() -> processo.finalizar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Processo seletivo já foi finalizado");
    }

    @Test
    void deveAvancarParaProximaEtapa() {
        ProcessoSeletivo processo = criarProcessoPadrao();
        UUID proximaEtapaId = UUID.randomUUID();
        
        ProcessoSeletivo avancado = processo.avancarParaEtapa(proximaEtapaId);

        assertThat(processo.getEtapaProcessoAtualId()).isNotEqualTo(proximaEtapaId);
        assertThat(avancado.getEtapaProcessoAtualId()).isEqualTo(proximaEtapaId);
        assertThat(avancado.getDataUltimaMudanca()).isAfter(processo.getDataUltimaMudanca());
    }

    @Test
    void naoDeveAvancarProcessoJaFinalizado() {
        ProcessoSeletivo processo = criarProcessoPadrao().finalizar();
        UUID proximaEtapaId = UUID.randomUUID();

        assertThatThrownBy(() -> processo.avancarParaEtapa(proximaEtapaId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Não é possível avançar um processo já finalizado");
    }

    @Test
    void deveVerificarSeProcessoEstaEmAndamento() {
        ProcessoSeletivo processo = criarProcessoPadrao();

        assertThat(processo.isEmAndamento()).isTrue();
        assertThat(processo.isFinalizado()).isFalse();
    }

    @Test
    void deveVerificarSeProcessoEstaFinalizado() {
        ProcessoSeletivo processo = criarProcessoPadrao().finalizar();

        assertThat(processo.isFinalizado()).isTrue();
        assertThat(processo.isEmAndamento()).isFalse();
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID candidaturaId = UUID.randomUUID();
        UUID etapaId = UUID.randomUUID();

        assertThatThrownBy(() -> ProcessoSeletivo.novo(null, etapaId))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("candidaturaId must not be null");

        assertThatThrownBy(() -> ProcessoSeletivo.novo(candidaturaId, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("etapaProcessoAtualId must not be null");
    }

    @Test
    void deveValidarCamposObrigatoriosAoRehydratar() {
        UUID id = UUID.randomUUID();
        UUID candidaturaId = UUID.randomUUID();
        UUID etapaId = UUID.randomUUID();
        LocalDateTime dataInicio = LocalDateTime.now();
        LocalDateTime dataUltimaMudanca = LocalDateTime.now();

        assertThatThrownBy(() -> ProcessoSeletivo.rehydrate(null, candidaturaId, etapaId,
                dataInicio, null, dataUltimaMudanca))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ProcessoSeletivo.rehydrate(id, null, etapaId,
                dataInicio, null, dataUltimaMudanca))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ProcessoSeletivo.rehydrate(id, candidaturaId, null,
                dataInicio, null, dataUltimaMudanca))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ProcessoSeletivo.rehydrate(id, candidaturaId, etapaId,
                null, null, dataUltimaMudanca))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ProcessoSeletivo.rehydrate(id, candidaturaId, etapaId,
                dataInicio, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    private ProcessoSeletivo criarProcessoPadrao() {
        UUID candidaturaId = UUID.randomUUID();
        UUID etapaInicialId = UUID.randomUUID();
        return ProcessoSeletivo.novo(candidaturaId, etapaInicialId);
    }
}

