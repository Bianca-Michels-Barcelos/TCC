package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ProjetoExperienciaTest {

    @Test
    void deveCriarNovoProjetoComAtributosPadrao() {
        UUID id = UUID.randomUUID();
        UUID experienciaId = UUID.randomUUID();
        String nome = "Sistema de Gestão";
        String descricao = "Desenvolvimento de sistema completo";

        ProjetoExperiencia projeto = ProjetoExperiencia.novo(id, experienciaId, nome, descricao);

        assertThat(projeto).isNotNull();
        assertThat(projeto.getId()).isEqualTo(id);
        assertThat(projeto.getExperienciaProfissionalId()).isEqualTo(experienciaId);
        assertThat(projeto.getNome()).isEqualTo(nome);
        assertThat(projeto.getDescricao()).isEqualTo(descricao);
        assertThat(projeto.isAtivo()).isTrue();
    }

    @Test
    void deveRehydratarProjetoExistente() {
        UUID id = UUID.randomUUID();
        UUID experienciaId = UUID.randomUUID();

        ProjetoExperiencia projeto = ProjetoExperiencia.rehydrate(id, experienciaId, 
                "Projeto X", "Descrição do projeto", false);

        assertThat(projeto.getId()).isEqualTo(id);
        assertThat(projeto.getExperienciaProfissionalId()).isEqualTo(experienciaId);
        assertThat(projeto.getNome()).isEqualTo("Projeto X");
        assertThat(projeto.getDescricao()).isEqualTo("Descrição do projeto");
        assertThat(projeto.isAtivo()).isFalse();
    }

    @Test
    void deveAtualizarNomeMantendoImutabilidade() {
        ProjetoExperiencia original = criarProjetoPadrao();
        
        ProjetoExperiencia atualizado = original.comNome("Novo Nome");

        assertThat(original.getNome()).isEqualTo("Sistema de Gestão");
        assertThat(atualizado.getNome()).isEqualTo("Novo Nome");
        assertThat(atualizado.getId()).isEqualTo(original.getId());
    }

    @Test
    void deveAtualizarDescricaoMantendoImutabilidade() {
        ProjetoExperiencia original = criarProjetoPadrao();
        
        ProjetoExperiencia atualizado = original.comDescricao("Nova descrição");

        assertThat(original.getDescricao()).isEqualTo("Desenvolvimento de sistema completo");
        assertThat(atualizado.getDescricao()).isEqualTo("Nova descrição");
    }

    @Test
    void deveAtivarProjeto() {
        ProjetoExperiencia projeto = criarProjetoPadrao().desativar();
        
        ProjetoExperiencia ativado = projeto.ativar();

        assertThat(projeto.isAtivo()).isFalse();
        assertThat(ativado.isAtivo()).isTrue();
    }

    @Test
    void deveRetornarMesmaInstanciaSeProjetoJaAtivo() {
        ProjetoExperiencia projeto = criarProjetoPadrao();
        
        ProjetoExperiencia resultado = projeto.ativar();

        assertThat(resultado).isSameAs(projeto);
    }

    @Test
    void deveDesativarProjeto() {
        ProjetoExperiencia projeto = criarProjetoPadrao();
        
        ProjetoExperiencia desativado = projeto.desativar();

        assertThat(projeto.isAtivo()).isTrue();
        assertThat(desativado.isAtivo()).isFalse();
    }

    @Test
    void deveRetornarMesmaInstanciaSeProjetoJaInativo() {
        ProjetoExperiencia projeto = criarProjetoPadrao().desativar();
        
        ProjetoExperiencia resultado = projeto.desativar();

        assertThat(resultado).isSameAs(projeto);
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID id = UUID.randomUUID();
        UUID experienciaId = UUID.randomUUID();

        assertThatThrownBy(() -> ProjetoExperiencia.novo(null, experienciaId, "Nome", "Descrição"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("id must not be null");

        assertThatThrownBy(() -> ProjetoExperiencia.novo(id, null, "Nome", "Descrição"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("experienciaProfissionalId must not be null");

        assertThatThrownBy(() -> ProjetoExperiencia.novo(id, experienciaId, null, "Descrição"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome must not be blank");

        assertThatThrownBy(() -> ProjetoExperiencia.novo(id, experienciaId, "   ", "Descrição"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome must not be blank");

        assertThatThrownBy(() -> ProjetoExperiencia.novo(id, experienciaId, "Nome", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descricao must not be blank");

        assertThatThrownBy(() -> ProjetoExperiencia.novo(id, experienciaId, "Nome", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descricao must not be blank");
    }

    @Test
    void deveValidarTamanhoMaximoDoNome() {
        UUID id = UUID.randomUUID();
        UUID experienciaId = UUID.randomUUID();
        String nomeLongo = "a".repeat(81);

        assertThatThrownBy(() -> ProjetoExperiencia.novo(id, experienciaId, nomeLongo, "Descrição"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome max 80 chars");
    }

    private ProjetoExperiencia criarProjetoPadrao() {
        UUID id = UUID.randomUUID();
        UUID experienciaId = UUID.randomUUID();
        return ProjetoExperiencia.novo(id, experienciaId, "Sistema de Gestão", 
                "Desenvolvimento de sistema completo");
    }
}

