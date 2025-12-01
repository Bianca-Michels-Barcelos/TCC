package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class HistoricoAcademicoTest {

    @Test
    void deveCriarNovoHistoricoComAtributosPadrao() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        String titulo = "Bacharel em Ciência da Computação";
        String descricao = "Formação completa";
        String instituicao = "Universidade Federal";
        LocalDate dataInicio = LocalDate.of(2016, 3, 1);
        LocalDate dataFim = LocalDate.of(2020, 12, 15);

        HistoricoAcademico historico = HistoricoAcademico.novo(id, usuarioId, titulo, 
                descricao, instituicao, dataInicio, dataFim);

        assertThat(historico).isNotNull();
        assertThat(historico.getId()).isEqualTo(id);
        assertThat(historico.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(historico.getTitulo()).isEqualTo(titulo);
        assertThat(historico.getDescricao()).isEqualTo(descricao);
        assertThat(historico.getInstituicao()).isEqualTo(instituicao);
        assertThat(historico.getDataInicio()).isEqualTo(dataInicio);
        assertThat(historico.getDataFim()).isEqualTo(dataFim);
        assertThat(historico.isAtivo()).isTrue();
    }

    @Test
    void deveCriarHistoricoSemDataFim() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        LocalDate dataInicio = LocalDate.of(2023, 1, 1);

        HistoricoAcademico historico = HistoricoAcademico.novo(id, usuarioId, 
                "Mestrado", "Cursando", "Universidade", dataInicio, null);

        assertThat(historico.getDataFim()).isNull();
    }

    @Test
    void deveRehydratarHistoricoExistente() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        LocalDate dataInicio = LocalDate.of(2016, 3, 1);
        LocalDate dataFim = LocalDate.of(2020, 12, 15);

        HistoricoAcademico historico = HistoricoAcademico.rehydrate(id, usuarioId, 
                "Título", "Descrição", "Instituição", dataInicio, dataFim, false);

        assertThat(historico.getId()).isEqualTo(id);
        assertThat(historico.isAtivo()).isFalse();
    }

    @Test
    void deveAtualizarTituloMantendoImutabilidade() {
        HistoricoAcademico original = criarHistoricoPadrao();
        
        HistoricoAcademico atualizado = original.comTitulo("Novo Título");

        assertThat(original.getTitulo()).isEqualTo("Bacharel em Ciência da Computação");
        assertThat(atualizado.getTitulo()).isEqualTo("Novo Título");
        assertThat(atualizado.getId()).isEqualTo(original.getId());
    }

    @Test
    void deveAtualizarDescricaoMantendoImutabilidade() {
        HistoricoAcademico original = criarHistoricoPadrao();
        
        HistoricoAcademico atualizado = original.comDescricao("Nova descrição");

        assertThat(original.getDescricao()).isEqualTo("Formação completa");
        assertThat(atualizado.getDescricao()).isEqualTo("Nova descrição");
    }

    @Test
    void deveAtualizarInstituicaoMantendoImutabilidade() {
        HistoricoAcademico original = criarHistoricoPadrao();
        
        HistoricoAcademico atualizado = original.comInstituicao("Nova Instituição");

        assertThat(original.getInstituicao()).isEqualTo("Universidade Federal");
        assertThat(atualizado.getInstituicao()).isEqualTo("Nova Instituição");
    }

    @Test
    void deveAtualizarDataInicioMantendoImutabilidade() {
        HistoricoAcademico original = criarHistoricoPadrao();
        LocalDate novaData = LocalDate.of(2017, 1, 1);
        
        HistoricoAcademico atualizado = original.comDataInicio(novaData);

        assertThat(original.getDataInicio()).isEqualTo(LocalDate.of(2016, 3, 1));
        assertThat(atualizado.getDataInicio()).isEqualTo(novaData);
    }

    @Test
    void deveAtualizarDataFimMantendoImutabilidade() {
        HistoricoAcademico original = criarHistoricoPadrao();
        LocalDate novaData = LocalDate.of(2021, 6, 30);
        
        HistoricoAcademico atualizado = original.comDataFim(novaData);

        assertThat(original.getDataFim()).isEqualTo(LocalDate.of(2020, 12, 15));
        assertThat(atualizado.getDataFim()).isEqualTo(novaData);
    }

    @Test
    void deveAtivarHistorico() {
        HistoricoAcademico historico = criarHistoricoPadrao().desativar();
        
        HistoricoAcademico ativado = historico.ativar();

        assertThat(historico.isAtivo()).isFalse();
        assertThat(ativado.isAtivo()).isTrue();
    }

    @Test
    void deveRetornarMesmaInstanciaSeHistoricoJaAtivo() {
        HistoricoAcademico historico = criarHistoricoPadrao();
        
        HistoricoAcademico resultado = historico.ativar();

        assertThat(resultado).isSameAs(historico);
    }

    @Test
    void deveDesativarHistorico() {
        HistoricoAcademico historico = criarHistoricoPadrao();
        
        HistoricoAcademico desativado = historico.desativar();

        assertThat(historico.isAtivo()).isTrue();
        assertThat(desativado.isAtivo()).isFalse();
    }

    @Test
    void deveRetornarMesmaInstanciaSeHistoricoJaInativo() {
        HistoricoAcademico historico = criarHistoricoPadrao().desativar();
        
        HistoricoAcademico resultado = historico.desativar();

        assertThat(resultado).isSameAs(historico);
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        LocalDate dataInicio = LocalDate.of(2016, 1, 1);

        assertThatThrownBy(() -> HistoricoAcademico.novo(null, usuarioId, "Título", 
                "Descrição", "Instituição", dataInicio, null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> HistoricoAcademico.novo(id, null, "Título", 
                "Descrição", "Instituição", dataInicio, null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> HistoricoAcademico.novo(id, usuarioId, null, 
                "Descrição", "Instituição", dataInicio, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("titulo must not be blank");

        assertThatThrownBy(() -> HistoricoAcademico.novo(id, usuarioId, "   ", 
                "Descrição", "Instituição", dataInicio, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("titulo must not be blank");

        assertThatThrownBy(() -> HistoricoAcademico.novo(id, usuarioId, "Título", 
                "Descrição", null, dataInicio, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("instituicao must not be blank");

        assertThatThrownBy(() -> HistoricoAcademico.novo(id, usuarioId, "Título", 
                "Descrição", "   ", dataInicio, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("instituicao must not be blank");

        assertThatThrownBy(() -> HistoricoAcademico.novo(id, usuarioId, "Título", 
                "Descrição", "Instituição", null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deveValidarTamanhoMaximoDoTitulo() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        String tituloLongo = "a".repeat(81);
        LocalDate dataInicio = LocalDate.of(2016, 1, 1);

        assertThatThrownBy(() -> HistoricoAcademico.novo(id, usuarioId, tituloLongo, 
                "Descrição", "Instituição", dataInicio, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("titulo max 80 chars");
    }

    @Test
    void deveValidarTamanhoMaximoDaInstituicao() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        String instituicaoLonga = "a".repeat(81);
        LocalDate dataInicio = LocalDate.of(2016, 1, 1);

        assertThatThrownBy(() -> HistoricoAcademico.novo(id, usuarioId, "Título", 
                "Descrição", instituicaoLonga, dataInicio, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("instituicao max 80 chars");
    }

    @Test
    void deveValidarDataFimPosteriorADataInicio() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        LocalDate dataInicio = LocalDate.of(2020, 1, 1);
        LocalDate dataFim = LocalDate.of(2019, 1, 1);

        assertThatThrownBy(() -> HistoricoAcademico.novo(id, usuarioId, "Título", 
                "Descrição", "Instituição", dataInicio, dataFim))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dataFim must be after dataInicio");
    }

    private HistoricoAcademico criarHistoricoPadrao() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        LocalDate dataInicio = LocalDate.of(2016, 3, 1);
        LocalDate dataFim = LocalDate.of(2020, 12, 15);
        return HistoricoAcademico.novo(id, usuarioId, "Bacharel em Ciência da Computação", 
                "Formação completa", "Universidade Federal", dataInicio, dataFim);
    }
}

