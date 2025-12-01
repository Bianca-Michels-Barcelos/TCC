package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class HistoricoEtapaProcessoTest {

    @Test
    void deveCriarNovoHistoricoComAtributosPadrao() {
        UUID processoId = UUID.randomUUID();
        UUID etapaAnteriorId = UUID.randomUUID();
        UUID etapaNovaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        String feedback = "Candidato aprovado para próxima etapa";

        HistoricoEtapaProcesso historico = HistoricoEtapaProcesso.novo(processoId, etapaAnteriorId, 
                etapaNovaId, usuarioId, feedback);

        assertThat(historico).isNotNull();
        assertThat(historico.getId()).isNotNull();
        assertThat(historico.getProcessoId()).isEqualTo(processoId);
        assertThat(historico.getEtapaAnteriorId()).isEqualTo(etapaAnteriorId);
        assertThat(historico.getEtapaNovaId()).isEqualTo(etapaNovaId);
        assertThat(historico.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(historico.getFeedback()).isEqualTo(feedback);
        assertThat(historico.getDataMudanca()).isNotNull();
    }

    @Test
    void deveCriarHistoricoSemEtapaAnterior() {
        UUID processoId = UUID.randomUUID();
        UUID etapaNovaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        HistoricoEtapaProcesso historico = HistoricoEtapaProcesso.novo(processoId, null, 
                etapaNovaId, usuarioId, "Processo iniciado");

        assertThat(historico.getEtapaAnteriorId()).isNull();
        assertThat(historico.isPrimeiraMudanca()).isTrue();
    }

    @Test
    void deveRehydratarHistoricoExistente() {
        UUID id = UUID.randomUUID();
        UUID processoId = UUID.randomUUID();
        UUID etapaAnteriorId = UUID.randomUUID();
        UUID etapaNovaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        LocalDateTime dataMudanca = LocalDateTime.of(2025, 1, 15, 10, 0);

        HistoricoEtapaProcesso historico = HistoricoEtapaProcesso.rehydrate(id, processoId, 
                etapaAnteriorId, etapaNovaId, usuarioId, "Feedback", dataMudanca);

        assertThat(historico.getId()).isEqualTo(id);
        assertThat(historico.getDataMudanca()).isEqualTo(dataMudanca);
    }

    @Test
    void deveVerificarSePrimeiraMudanca() {
        UUID processoId = UUID.randomUUID();
        UUID etapaNovaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        HistoricoEtapaProcesso primeira = HistoricoEtapaProcesso.novo(processoId, null, 
                etapaNovaId, usuarioId, "Primeira mudança");
        HistoricoEtapaProcesso segunda = HistoricoEtapaProcesso.novo(processoId, UUID.randomUUID(), 
                etapaNovaId, usuarioId, "Segunda mudança");

        assertThat(primeira.isPrimeiraMudanca()).isTrue();
        assertThat(segunda.isPrimeiraMudanca()).isFalse();
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID processoId = UUID.randomUUID();
        UUID etapaNovaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        assertThatThrownBy(() -> HistoricoEtapaProcesso.novo(null, null, etapaNovaId, 
                usuarioId, "Feedback"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> HistoricoEtapaProcesso.novo(processoId, null, null, 
                usuarioId, "Feedback"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> HistoricoEtapaProcesso.novo(processoId, null, etapaNovaId, 
                null, "Feedback"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> HistoricoEtapaProcesso.novo(processoId, null, etapaNovaId, 
                usuarioId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("feedback must not be null or blank");

        assertThatThrownBy(() -> HistoricoEtapaProcesso.novo(processoId, null, etapaNovaId, 
                usuarioId, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("feedback must not be null or blank");
    }

    @Test
    void deveValidarCamposObrigatoriosAoRehydratar() {
        UUID id = UUID.randomUUID();
        UUID processoId = UUID.randomUUID();
        UUID etapaNovaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        LocalDateTime dataMudanca = LocalDateTime.now();

        assertThatThrownBy(() -> HistoricoEtapaProcesso.rehydrate(null, processoId, null, 
                etapaNovaId, usuarioId, "Feedback", dataMudanca))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> HistoricoEtapaProcesso.rehydrate(id, null, null, 
                etapaNovaId, usuarioId, "Feedback", dataMudanca))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> HistoricoEtapaProcesso.rehydrate(id, processoId, null, 
                null, usuarioId, "Feedback", dataMudanca))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> HistoricoEtapaProcesso.rehydrate(id, processoId, null, 
                etapaNovaId, null, "Feedback", dataMudanca))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> HistoricoEtapaProcesso.rehydrate(id, processoId, null, 
                etapaNovaId, usuarioId, "Feedback", null))
                .isInstanceOf(NullPointerException.class);
    }
}

