package com.barcelos.recrutamento.core.model;

import com.barcelos.recrutamento.data.entity.NivelCompetencia;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class CompetenciaTest {

    @Test
    void deveCriarNovaCompetenciaComAtributosPadrao() {
        UUID perfilId = UUID.randomUUID();
        String titulo = "Java";
        String descricao = "Programação Java avançada";
        NivelCompetencia nivel = NivelCompetencia.AVANCADO;

        Competencia competencia = Competencia.nova(perfilId, titulo, descricao, nivel);

        assertThat(competencia).isNotNull();
        assertThat(competencia.getId()).isNotNull();
        assertThat(competencia.getPerfilCandidatoId()).isEqualTo(perfilId);
        assertThat(competencia.getTitulo()).isEqualTo(titulo);
        assertThat(competencia.getDescricao()).isEqualTo(descricao);
        assertThat(competencia.getNivel()).isEqualTo(nivel);
        assertThat(competencia.isAtivo()).isTrue();
    }

    @Test
    void deveRehydratarCompetenciaExistente() {
        UUID id = UUID.randomUUID();
        UUID perfilId = UUID.randomUUID();

        Competencia competencia = Competencia.rehydrate(id, perfilId, "Python", 
                "Programação Python", NivelCompetencia.INTERMEDIARIO, false);

        assertThat(competencia.getId()).isEqualTo(id);
        assertThat(competencia.getPerfilCandidatoId()).isEqualTo(perfilId);
        assertThat(competencia.getTitulo()).isEqualTo("Python");
        assertThat(competencia.getNivel()).isEqualTo(NivelCompetencia.INTERMEDIARIO);
        assertThat(competencia.isAtivo()).isFalse();
    }

    @Test
    void deveAtualizarTituloMantendoImutabilidade() {
        Competencia original = criarCompetenciaPadrao();
        
        Competencia atualizada = original.comTitulo("Kotlin");

        assertThat(original.getTitulo()).isEqualTo("Java");
        assertThat(atualizada.getTitulo()).isEqualTo("Kotlin");
        assertThat(atualizada.getId()).isEqualTo(original.getId());
    }

    @Test
    void deveAtualizarDescricaoMantendoImutabilidade() {
        Competencia original = criarCompetenciaPadrao();
        
        Competencia atualizada = original.comDescricao("Nova descrição");

        assertThat(original.getDescricao()).isEqualTo("Programação Java avançada");
        assertThat(atualizada.getDescricao()).isEqualTo("Nova descrição");
    }

    @Test
    void deveAtualizarNivelMantendoImutabilidade() {
        Competencia original = criarCompetenciaPadrao();
        
        Competencia atualizada = original.comNivel(NivelCompetencia.BASICO);

        assertThat(original.getNivel()).isEqualTo(NivelCompetencia.AVANCADO);
        assertThat(atualizada.getNivel()).isEqualTo(NivelCompetencia.BASICO);
    }

    @Test
    void deveAtivarCompetencia() {
        Competencia competencia = criarCompetenciaPadrao().desativar();
        
        Competencia ativada = competencia.ativar();

        assertThat(competencia.isAtivo()).isFalse();
        assertThat(ativada.isAtivo()).isTrue();
    }

    @Test
    void deveRetornarMesmaInstanciaSeCompetenciaJaAtiva() {
        Competencia competencia = criarCompetenciaPadrao();
        
        Competencia resultado = competencia.ativar();

        assertThat(resultado).isSameAs(competencia);
    }

    @Test
    void deveDesativarCompetencia() {
        Competencia competencia = criarCompetenciaPadrao();
        
        Competencia desativada = competencia.desativar();

        assertThat(competencia.isAtivo()).isTrue();
        assertThat(desativada.isAtivo()).isFalse();
    }

    @Test
    void deveRetornarMesmaInstanciaSeCompetenciaJaInativa() {
        Competencia competencia = criarCompetenciaPadrao().desativar();
        
        Competencia resultado = competencia.desativar();

        assertThat(resultado).isSameAs(competencia);
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID perfilId = UUID.randomUUID();

        assertThatThrownBy(() -> Competencia.nova(null, "Título", "Descrição", 
                NivelCompetencia.BASICO))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> Competencia.nova(perfilId, null, "Descrição", 
                NivelCompetencia.BASICO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("titulo must not be blank");

        assertThatThrownBy(() -> Competencia.nova(perfilId, "   ", "Descrição", 
                NivelCompetencia.BASICO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("titulo must not be blank");

        assertThatThrownBy(() -> Competencia.nova(perfilId, "Título", null, 
                NivelCompetencia.BASICO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descricao must not be blank");

        assertThatThrownBy(() -> Competencia.nova(perfilId, "Título", "   ", 
                NivelCompetencia.BASICO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descricao must not be blank");

        assertThatThrownBy(() -> Competencia.nova(perfilId, "Título", "Descrição", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deveValidarTamanhoMaximoDoTitulo() {
        UUID perfilId = UUID.randomUUID();
        String tituloLongo = "a".repeat(81);

        assertThatThrownBy(() -> Competencia.nova(perfilId, tituloLongo, "Descrição", 
                NivelCompetencia.BASICO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("titulo max 80 chars");
    }

    @Test
    void deveCriarCompetenciaComDiferentesNiveis() {
        UUID perfilId = UUID.randomUUID();

        Competencia basico = Competencia.nova(perfilId, "HTML", "Markup", 
                NivelCompetencia.BASICO);
        Competencia intermediario = Competencia.nova(perfilId, "CSS", "Estilização", 
                NivelCompetencia.INTERMEDIARIO);
        Competencia avancado = Competencia.nova(perfilId, "JavaScript", "Programação", 
                NivelCompetencia.AVANCADO);

        assertThat(basico.getNivel()).isEqualTo(NivelCompetencia.BASICO);
        assertThat(intermediario.getNivel()).isEqualTo(NivelCompetencia.INTERMEDIARIO);
        assertThat(avancado.getNivel()).isEqualTo(NivelCompetencia.AVANCADO);
    }

    private Competencia criarCompetenciaPadrao() {
        UUID perfilId = UUID.randomUUID();
        return Competencia.nova(perfilId, "Java", "Programação Java avançada", 
                NivelCompetencia.AVANCADO);
    }
}

