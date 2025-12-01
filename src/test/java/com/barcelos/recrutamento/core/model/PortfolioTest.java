package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class PortfolioTest {

    @Test
    void deveCriarNovoPortfolioComAtributosPadrao() {
        UUID usuarioId = UUID.randomUUID();
        String titulo = "Meu Portfolio";
        String link = "https://github.com/usuario/portfolio";

        Portfolio portfolio = Portfolio.novo(usuarioId, titulo, link);

        assertThat(portfolio).isNotNull();
        assertThat(portfolio.getId()).isNotNull();
        assertThat(portfolio.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(portfolio.getTitulo()).isEqualTo(titulo);
        assertThat(portfolio.getLink()).isEqualTo(link);
        assertThat(portfolio.isAtivo()).isTrue();
    }

    @Test
    void deveRehydratarPortfolioExistente() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        Portfolio portfolio = Portfolio.rehydrate(id, usuarioId, "Projeto X", 
                "https://example.com", false);

        assertThat(portfolio.getId()).isEqualTo(id);
        assertThat(portfolio.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(portfolio.getTitulo()).isEqualTo("Projeto X");
        assertThat(portfolio.getLink()).isEqualTo("https://example.com");
        assertThat(portfolio.isAtivo()).isFalse();
    }

    @Test
    void deveAtualizarTituloMantendoImutabilidade() {
        Portfolio original = criarPortfolioPadrao();
        
        Portfolio atualizado = original.comTitulo("Novo Título");

        assertThat(original.getTitulo()).isEqualTo("Meu Portfolio");
        assertThat(atualizado.getTitulo()).isEqualTo("Novo Título");
        assertThat(atualizado.getId()).isEqualTo(original.getId());
    }

    @Test
    void deveAtualizarLinkMantendoImutabilidade() {
        Portfolio original = criarPortfolioPadrao();
        
        Portfolio atualizado = original.comLink("https://novo-link.com");

        assertThat(original.getLink()).isEqualTo("https://github.com/usuario/portfolio");
        assertThat(atualizado.getLink()).isEqualTo("https://novo-link.com");
    }

    @Test
    void deveAtivarPortfolio() {
        Portfolio portfolio = criarPortfolioPadrao().desativar();
        
        Portfolio ativado = portfolio.ativar();

        assertThat(portfolio.isAtivo()).isFalse();
        assertThat(ativado.isAtivo()).isTrue();
    }

    @Test
    void deveRetornarMesmaInstanciaSePortfolioJaAtivo() {
        Portfolio portfolio = criarPortfolioPadrao();
        
        Portfolio resultado = portfolio.ativar();

        assertThat(resultado).isSameAs(portfolio);
    }

    @Test
    void deveDesativarPortfolio() {
        Portfolio portfolio = criarPortfolioPadrao();
        
        Portfolio desativado = portfolio.desativar();

        assertThat(portfolio.isAtivo()).isTrue();
        assertThat(desativado.isAtivo()).isFalse();
    }

    @Test
    void deveRetornarMesmaInstanciaSePortfolioJaInativo() {
        Portfolio portfolio = criarPortfolioPadrao().desativar();
        
        Portfolio resultado = portfolio.desativar();

        assertThat(resultado).isSameAs(portfolio);
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID usuarioId = UUID.randomUUID();

        assertThatThrownBy(() -> Portfolio.novo(null, "Título", "https://link.com"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("usuarioId must not be null");

        assertThatThrownBy(() -> Portfolio.novo(usuarioId, null, "https://link.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("titulo must not be blank");

        assertThatThrownBy(() -> Portfolio.novo(usuarioId, "   ", "https://link.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("titulo must not be blank");

        assertThatThrownBy(() -> Portfolio.novo(usuarioId, "Título", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("link must not be blank");

        assertThatThrownBy(() -> Portfolio.novo(usuarioId, "Título", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("link must not be blank");
    }

    @Test
    void deveValidarTamanhoMaximoDoTitulo() {
        UUID usuarioId = UUID.randomUUID();
        String tituloLongo = "a".repeat(101);

        assertThatThrownBy(() -> Portfolio.novo(usuarioId, tituloLongo, "https://link.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("titulo max 100 chars");
    }

    @Test
    void deveValidarTamanhoMaximoDoLink() {
        UUID usuarioId = UUID.randomUUID();
        String linkLongo = "https://example.com/" + "a".repeat(256);

        assertThatThrownBy(() -> Portfolio.novo(usuarioId, "Título", linkLongo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("link max 255 chars");
    }

    private Portfolio criarPortfolioPadrao() {
        UUID usuarioId = UUID.randomUUID();
        return Portfolio.novo(usuarioId, "Meu Portfolio", "https://github.com/usuario/portfolio");
    }
}

