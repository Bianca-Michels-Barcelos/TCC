package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.model.ModeloCurriculoEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CurriculoPDFServiceTest {

    @InjectMocks
    private CurriculoPDFService service;

    private String htmlConteudo;
    private String nomeArquivo;

    @BeforeEach
    void setUp() {
        htmlConteudo = "<h1>João Silva</h1><p>Desenvolvedor Java</p>";
        nomeArquivo = System.getProperty("java.io.tmpdir") + "/test-curriculo.pdf";
    }

    @Test
    void deveGerarPDFComSucesso() {
        File resultado = service.gerarPDF(htmlConteudo, ModeloCurriculoEnum.PROFISSIONAL, nomeArquivo);

        assertThat(resultado).isNotNull();
        assertThat(resultado.exists()).isTrue();
        assertThat(resultado.getName()).endsWith(".pdf");
        
        resultado.delete();
    }

    @Test
    void deveGerarPDFComModeloProfissional() {
        File resultado = service.gerarPDF(htmlConteudo, ModeloCurriculoEnum.PROFISSIONAL, nomeArquivo);

        assertThat(resultado.exists()).isTrue();
        resultado.delete();
    }

    @Test
    void deveGerarPDFComModeloCriativo() {
        String nomeArquivo2 = System.getProperty("java.io.tmpdir") + "/test-curriculo-criativo.pdf";
        File resultado = service.gerarPDF(htmlConteudo, ModeloCurriculoEnum.CRIATIVO, nomeArquivo2);

        assertThat(resultado.exists()).isTrue();
        resultado.delete();
    }

    @Test
    void deveGerarPDFComModeloExecutivo() {
        String nomeArquivo3 = System.getProperty("java.io.tmpdir") + "/test-curriculo-executivo.pdf";
        File resultado = service.gerarPDF(htmlConteudo, ModeloCurriculoEnum.EXECUTIVO, nomeArquivo3);

        assertThat(resultado.exists()).isTrue();
        resultado.delete();
    }

    @Test
    void deveGerarPDFComModeloAcademico() {
        String nomeArquivo4 = System.getProperty("java.io.tmpdir") + "/test-curriculo-academico.pdf";
        File resultado = service.gerarPDF(htmlConteudo, ModeloCurriculoEnum.ACADEMICO, nomeArquivo4);

        assertThat(resultado.exists()).isTrue();
        resultado.delete();
    }

    @Test
    void deveCriarDiretorioSeNaoExiste() {
        String nomeArquivoComDiretorio = System.getProperty("java.io.tmpdir") + "/test-dir/curriculo.pdf";
        
        File resultado = service.gerarPDF(htmlConteudo, ModeloCurriculoEnum.PROFISSIONAL, nomeArquivoComDiretorio);

        assertThat(resultado.exists()).isTrue();
        resultado.delete();
        new File(System.getProperty("java.io.tmpdir") + "/test-dir").delete();
    }
}

