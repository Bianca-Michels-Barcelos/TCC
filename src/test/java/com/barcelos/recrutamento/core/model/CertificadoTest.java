package com.barcelos.recrutamento.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class CertificadoTest {

    @Test
    void deveCriarNovoCertificadoComAtributosPadrao() {
        UUID perfilId = UUID.randomUUID();
        String titulo = "AWS Certified Solutions Architect";
        String instituicao = "Amazon Web Services";
        LocalDate dataEmissao = LocalDate.of(2023, 6, 15);
        LocalDate dataValidade = LocalDate.of(2026, 6, 15);
        String descricao = "Certificação AWS";

        Certificado certificado = Certificado.novo(perfilId, titulo, instituicao, 
                dataEmissao, dataValidade, descricao);

        assertThat(certificado).isNotNull();
        assertThat(certificado.getId()).isNotNull();
        assertThat(certificado.getPerfilCandidatoId()).isEqualTo(perfilId);
        assertThat(certificado.getTitulo()).isEqualTo(titulo);
        assertThat(certificado.getInstituicao()).isEqualTo(instituicao);
        assertThat(certificado.getDataEmissao()).isEqualTo(dataEmissao);
        assertThat(certificado.getDataValidade()).isEqualTo(dataValidade);
        assertThat(certificado.getDescricao()).isEqualTo(descricao);
        assertThat(certificado.isAtivo()).isTrue();
    }

    @Test
    void deveCriarCertificadoSemDataValidade() {
        UUID perfilId = UUID.randomUUID();
        LocalDate dataEmissao = LocalDate.of(2023, 6, 15);

        Certificado certificado = Certificado.novo(perfilId, "Certificado", 
                "Instituição", dataEmissao, null, "Descrição");

        assertThat(certificado.getDataValidade()).isNull();
    }

    @Test
    void deveRehydratarCertificadoExistente() {
        UUID id = UUID.randomUUID();
        UUID perfilId = UUID.randomUUID();
        LocalDate dataEmissao = LocalDate.of(2023, 1, 1);
        LocalDate dataValidade = LocalDate.of(2025, 1, 1);

        Certificado certificado = Certificado.rehydrate(id, perfilId, "Título", 
                "Instituição", dataEmissao, dataValidade, "Descrição", false);

        assertThat(certificado.getId()).isEqualTo(id);
        assertThat(certificado.isAtivo()).isFalse();
    }

    @Test
    void deveAtualizarTituloMantendoImutabilidade() {
        Certificado original = criarCertificadoPadrao();
        
        Certificado atualizado = original.comTitulo("Novo Título");

        assertThat(original.getTitulo()).isEqualTo("AWS Certified Solutions Architect");
        assertThat(atualizado.getTitulo()).isEqualTo("Novo Título");
        assertThat(atualizado.getId()).isEqualTo(original.getId());
    }

    @Test
    void deveAtualizarInstituicaoMantendoImutabilidade() {
        Certificado original = criarCertificadoPadrao();
        
        Certificado atualizado = original.comInstituicao("Nova Instituição");

        assertThat(original.getInstituicao()).isEqualTo("Amazon Web Services");
        assertThat(atualizado.getInstituicao()).isEqualTo("Nova Instituição");
    }

    @Test
    void deveAtualizarDataEmissaoMantendoImutabilidade() {
        Certificado original = criarCertificadoPadrao();
        LocalDate novaData = LocalDate.of(2024, 1, 1);
        
        Certificado atualizado = original.comDataEmissao(novaData);

        assertThat(original.getDataEmissao()).isEqualTo(LocalDate.of(2023, 6, 15));
        assertThat(atualizado.getDataEmissao()).isEqualTo(novaData);
    }

    @Test
    void deveAtualizarDataValidadeMantendoImutabilidade() {
        Certificado original = criarCertificadoPadrao();
        LocalDate novaData = LocalDate.of(2027, 6, 15);
        
        Certificado atualizado = original.comDataValidade(novaData);

        assertThat(original.getDataValidade()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(atualizado.getDataValidade()).isEqualTo(novaData);
    }

    @Test
    void deveAtualizarDescricaoMantendoImutabilidade() {
        Certificado original = criarCertificadoPadrao();
        
        Certificado atualizado = original.comDescricao("Nova descrição");

        assertThat(original.getDescricao()).isEqualTo("Certificação AWS");
        assertThat(atualizado.getDescricao()).isEqualTo("Nova descrição");
    }

    @Test
    void deveAtivarCertificado() {
        Certificado certificado = criarCertificadoPadrao().desativar();
        
        Certificado ativado = certificado.ativar();

        assertThat(certificado.isAtivo()).isFalse();
        assertThat(ativado.isAtivo()).isTrue();
    }

    @Test
    void deveRetornarMesmaInstanciaSeCertificadoJaAtivo() {
        Certificado certificado = criarCertificadoPadrao();
        
        Certificado resultado = certificado.ativar();

        assertThat(resultado).isSameAs(certificado);
    }

    @Test
    void deveDesativarCertificado() {
        Certificado certificado = criarCertificadoPadrao();
        
        Certificado desativado = certificado.desativar();

        assertThat(certificado.isAtivo()).isTrue();
        assertThat(desativado.isAtivo()).isFalse();
    }

    @Test
    void deveRetornarMesmaInstanciaSeCertificadoJaInativo() {
        Certificado certificado = criarCertificadoPadrao().desativar();
        
        Certificado resultado = certificado.desativar();

        assertThat(resultado).isSameAs(certificado);
    }

    @Test
    void deveValidarCamposObrigatorios() {
        UUID perfilId = UUID.randomUUID();
        LocalDate dataEmissao = LocalDate.of(2023, 1, 1);

        assertThatThrownBy(() -> Certificado.novo(null, "Título", "Instituição", 
                dataEmissao, null, "Descrição"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> Certificado.novo(perfilId, null, "Instituição", 
                dataEmissao, null, "Descrição"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("titulo must not be blank");

        assertThatThrownBy(() -> Certificado.novo(perfilId, "   ", "Instituição", 
                dataEmissao, null, "Descrição"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("titulo must not be blank");

        assertThatThrownBy(() -> Certificado.novo(perfilId, "Título", null, 
                dataEmissao, null, "Descrição"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("instituicao must not be blank");

        assertThatThrownBy(() -> Certificado.novo(perfilId, "Título", "   ", 
                dataEmissao, null, "Descrição"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("instituicao must not be blank");

        assertThatThrownBy(() -> Certificado.novo(perfilId, "Título", "Instituição", 
                null, null, "Descrição"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deveValidarTamanhoMaximoDoTitulo() {
        UUID perfilId = UUID.randomUUID();
        String tituloLongo = "a".repeat(101);
        LocalDate dataEmissao = LocalDate.of(2023, 1, 1);

        assertThatThrownBy(() -> Certificado.novo(perfilId, tituloLongo, "Instituição", 
                dataEmissao, null, "Descrição"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("titulo max 100 chars");
    }

    @Test
    void deveValidarTamanhoMaximoDaInstituicao() {
        UUID perfilId = UUID.randomUUID();
        String instituicaoLonga = "a".repeat(101);
        LocalDate dataEmissao = LocalDate.of(2023, 1, 1);

        assertThatThrownBy(() -> Certificado.novo(perfilId, "Título", instituicaoLonga, 
                dataEmissao, null, "Descrição"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("instituicao max 100 chars");
    }

    @Test
    void deveValidarDataValidadePosteriorADataEmissao() {
        UUID perfilId = UUID.randomUUID();
        LocalDate dataEmissao = LocalDate.of(2023, 1, 1);
        LocalDate dataValidade = LocalDate.of(2022, 1, 1);

        assertThatThrownBy(() -> Certificado.novo(perfilId, "Título", "Instituição", 
                dataEmissao, dataValidade, "Descrição"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dataValidade must be after dataEmissao");
    }

    private Certificado criarCertificadoPadrao() {
        UUID perfilId = UUID.randomUUID();
        LocalDate dataEmissao = LocalDate.of(2023, 6, 15);
        LocalDate dataValidade = LocalDate.of(2026, 6, 15);
        return Certificado.novo(perfilId, "AWS Certified Solutions Architect", 
                "Amazon Web Services", dataEmissao, dataValidade, "Certificação AWS");
    }
}

