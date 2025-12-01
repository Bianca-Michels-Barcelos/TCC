package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class NivelExperienciaTest {

    @Test
    void deveCriarNovoNivelExperienciaComAtributosPadrao() {
        UUID organizacaoId = UUID.randomUUID();
        String descricao = "Júnior";

        NivelExperiencia nivel = NivelExperiencia.novo(organizacaoId, descricao);

        assertThat(nivel).isNotNull();
        assertThat(nivel.getId()).isNotNull();
        assertThat(nivel.getOrganizacaoId()).isEqualTo(organizacaoId);
        assertThat(nivel.getDescricao()).isEqualTo(descricao);
        assertThat(nivel.isAtivo()).isTrue();
    }

    @Test
    void deveRehydratarNivelExperienciaExistente() {
        UUID id = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();

        NivelExperiencia nivel = NivelExperiencia.rehydrate(id, organizacaoId, "Sênior", false);

        assertThat(nivel.getId()).isEqualTo(id);
        assertThat(nivel.getOrganizacaoId()).isEqualTo(organizacaoId);
        assertThat(nivel.getDescricao()).isEqualTo("Sênior");
        assertThat(nivel.isAtivo()).isFalse();
    }

    @Test
    void deveAtualizarNivelExperiencia() {
        UUID id = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();

        NivelExperiencia atualizado = NivelExperiencia.atualizar(id, organizacaoId, "Pleno");

        assertThat(atualizado.getId()).isEqualTo(id);
        assertThat(atualizado.getDescricao()).isEqualTo("Pleno");
        assertThat(atualizado.isAtivo()).isTrue();
    }

    @Test
    void deveAtualizarDescricaoMantendoImutabilidade() {
        NivelExperiencia original = criarNivelPadrao();
        
        NivelExperiencia atualizado = original.comDescricao("Especialista");

        assertThat(original.getDescricao()).isEqualTo("Júnior");
        assertThat(atualizado.getDescricao()).isEqualTo("Especialista");
        assertThat(atualizado.getId()).isEqualTo(original.getId());
    }

    @Test
    void deveAtivarNivel() {
        NivelExperiencia nivel = criarNivelPadrao().desativar();
        
        NivelExperiencia ativado = nivel.ativar();

        assertThat(nivel.isAtivo()).isFalse();
        assertThat(ativado.isAtivo()).isTrue();
    }

    @Test
    void deveRetornarMesmaInstanciaSeNivelJaAtivo() {
        NivelExperiencia nivel = criarNivelPadrao();
        
        NivelExperiencia resultado = nivel.ativar();

        assertThat(resultado).isSameAs(nivel);
    }

    @Test
    void deveDesativarNivel() {
        NivelExperiencia nivel = criarNivelPadrao();
        
        NivelExperiencia desativado = nivel.desativar();

        assertThat(nivel.isAtivo()).isTrue();
        assertThat(desativado.isAtivo()).isFalse();
    }

    @Test
    void deveRetornarMesmaInstanciaSeNivelJaInativo() {
        NivelExperiencia nivel = criarNivelPadrao().desativar();
        
        NivelExperiencia resultado = nivel.desativar();

        assertThat(resultado).isSameAs(nivel);
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID organizacaoId = UUID.randomUUID();

        assertThatThrownBy(() -> NivelExperiencia.novo(null, "Júnior"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> NivelExperiencia.novo(organizacaoId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descricao must not be blank");

        assertThatThrownBy(() -> NivelExperiencia.novo(organizacaoId, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descricao must not be blank");
    }

    @Test
    void deveValidarTamanhoMaximoDaDescricao() {
        UUID organizacaoId = UUID.randomUUID();
        String descricaoLonga = "a".repeat(51);

        assertThatThrownBy(() -> NivelExperiencia.novo(organizacaoId, descricaoLonga))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descricao must be at most 50 characters");
    }

    private NivelExperiencia criarNivelPadrao() {
        UUID organizacaoId = UUID.randomUUID();
        return NivelExperiencia.novo(organizacaoId, "Júnior");
    }
}
