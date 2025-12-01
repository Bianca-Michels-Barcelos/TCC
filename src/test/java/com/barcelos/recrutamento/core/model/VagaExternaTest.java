package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class VagaExternaTest {

    @Test
    void deveCriarNovaVagaExternaComAtributosPadrao() {
        String titulo = "Desenvolvedor Java";
        String descricao = "Vaga para desenvolvedor";
        String requisitos = "Conhecimento em Java";
        UUID candidatoId = UUID.randomUUID();

        VagaExterna vaga = VagaExterna.nova(titulo, descricao, requisitos, candidatoId);

        assertThat(vaga).isNotNull();
        assertThat(vaga.getId()).isNotNull();
        assertThat(vaga.getTitulo()).isEqualTo(titulo);
        assertThat(vaga.getDescricao()).isEqualTo(descricao);
        assertThat(vaga.getRequisitos()).isEqualTo(requisitos);
        assertThat(vaga.getCandidatoUsuarioId()).isEqualTo(candidatoId);
        assertThat(vaga.getArquivoCurriculo()).isNull();
        assertThat(vaga.getConteudoCurriculo()).isNull();
        assertThat(vaga.getModeloCurriculo()).isNull();
        assertThat(vaga.isAtivo()).isTrue();
        assertThat(vaga.getCriadoEm()).isNotNull();
    }

    @Test
    void deveRehydratarVagaExternaExistente() {
        UUID id = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        LocalDateTime criadoEm = LocalDateTime.of(2025, 1, 15, 10, 0);

        VagaExterna vaga = VagaExterna.rehydrate(id, "Título", "Descrição", "Requisitos",
                "curriculo.pdf", "Conteúdo", ModeloCurriculoEnum.PROFISSIONAL, candidatoId, false, criadoEm);

        assertThat(vaga.getId()).isEqualTo(id);
        assertThat(vaga.getArquivoCurriculo()).isEqualTo("curriculo.pdf");
        assertThat(vaga.getConteudoCurriculo()).isEqualTo("Conteúdo");
        assertThat(vaga.getModeloCurriculo()).isEqualTo(ModeloCurriculoEnum.PROFISSIONAL);
        assertThat(vaga.isAtivo()).isFalse();
        assertThat(vaga.getCriadoEm()).isEqualTo(criadoEm);
    }

    @Test
    void deveAtualizarArquivoCurriculoMantendoImutabilidade() {
        VagaExterna original = criarVagaExternaPadrao();
        
        VagaExterna atualizada = original.comArquivoCurriculo("novo-curriculo.pdf");

        assertThat(original.getArquivoCurriculo()).isNull();
        assertThat(atualizada.getArquivoCurriculo()).isEqualTo("novo-curriculo.pdf");
        assertThat(atualizada.getId()).isEqualTo(original.getId());
    }

    @Test
    void deveAtualizarCurriculoCompletoMantendoImutabilidade() {
        VagaExterna original = criarVagaExternaPadrao();
        
        VagaExterna atualizada = original.comCurriculo("Novo conteúdo", "arquivo.pdf", 
                ModeloCurriculoEnum.CRIATIVO);

        assertThat(original.getConteudoCurriculo()).isNull();
        assertThat(atualizada.getConteudoCurriculo()).isEqualTo("Novo conteúdo");
        assertThat(atualizada.getArquivoCurriculo()).isEqualTo("arquivo.pdf");
        assertThat(atualizada.getModeloCurriculo()).isEqualTo(ModeloCurriculoEnum.CRIATIVO);
    }

    @Test
    void deveAtualizarInformacoesVagaMantendoImutabilidade() {
        VagaExterna original = criarVagaExternaPadrao();
        
        VagaExterna atualizada = original.atualizar("Novo Título", "Nova Descrição", "Novos Requisitos");

        assertThat(original.getTitulo()).isEqualTo("Desenvolvedor Java");
        assertThat(atualizada.getTitulo()).isEqualTo("Novo Título");
        assertThat(atualizada.getDescricao()).isEqualTo("Nova Descrição");
        assertThat(atualizada.getRequisitos()).isEqualTo("Novos Requisitos");
    }

    @Test
    void deveAtualizarApenasInformacoesNaoNulas() {
        VagaExterna original = criarVagaExternaPadrao();
        
        VagaExterna atualizada = original.atualizar("Novo Título", null, null);

        assertThat(atualizada.getTitulo()).isEqualTo("Novo Título");
        assertThat(atualizada.getDescricao()).isEqualTo(original.getDescricao());
        assertThat(atualizada.getRequisitos()).isEqualTo(original.getRequisitos());
    }

    @Test
    void deveDesativarVagaExterna() {
        VagaExterna vaga = criarVagaExternaPadrao();
        
        VagaExterna desativada = vaga.desativar();

        assertThat(vaga.isAtivo()).isTrue();
        assertThat(desativada.isAtivo()).isFalse();
    }

    @Test
    void naoDeveDesativarVagaJaDesativada() {
        VagaExterna vaga = criarVagaExternaPadrao().desativar();

        assertThatThrownBy(() -> vaga.desativar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Vaga externa já está desativada");
    }

    @Test
    void deveReativarVagaExterna() {
        VagaExterna vaga = criarVagaExternaPadrao().desativar();
        
        VagaExterna reativada = vaga.reativar();

        assertThat(vaga.isAtivo()).isFalse();
        assertThat(reativada.isAtivo()).isTrue();
    }

    @Test
    void naoDeveReativarVagaJaAtiva() {
        VagaExterna vaga = criarVagaExternaPadrao();

        assertThatThrownBy(() -> vaga.reativar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Vaga externa já está ativa");
    }

    @Test
    void deveVerificarSePossuiCurriculo() {
        VagaExterna vagaSem = criarVagaExternaPadrao();
        VagaExterna vagaCom = vagaSem.comArquivoCurriculo("curriculo.pdf");
        VagaExterna vagaVazio = vagaSem.comArquivoCurriculo("   ");

        assertThat(vagaSem.possuiCurriculo()).isFalse();
        assertThat(vagaCom.possuiCurriculo()).isTrue();
        assertThat(vagaVazio.possuiCurriculo()).isFalse();
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID candidatoId = UUID.randomUUID();

        assertThatThrownBy(() -> VagaExterna.nova(null, "Descrição", "Requisitos", candidatoId))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("titulo must not be null");

        assertThatThrownBy(() -> VagaExterna.nova("Título", null, "Requisitos", candidatoId))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("descricao must not be null");

        assertThatThrownBy(() -> VagaExterna.nova("Título", "Descrição", null, candidatoId))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("requisitos must not be null");

        assertThatThrownBy(() -> VagaExterna.nova("Título", "Descrição", "Requisitos", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("candidatoUsuarioId must not be null");
    }

    private VagaExterna criarVagaExternaPadrao() {
        UUID candidatoId = UUID.randomUUID();
        return VagaExterna.nova("Desenvolvedor Java", "Vaga para desenvolvedor", 
                "Conhecimento em Java", candidatoId);
    }
}

