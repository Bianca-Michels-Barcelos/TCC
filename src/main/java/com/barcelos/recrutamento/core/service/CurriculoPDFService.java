package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.model.ModeloCurriculoEnum;
import com.lowagie.text.DocumentException;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Service
public class CurriculoPDFService {

    
    public File gerarPDF(String conteudoHtml, ModeloCurriculoEnum modelo, String nomeArquivo) {
        try {
            File file = new File(nomeArquivo);
            file.getParentFile().mkdirs();

            String htmlCompleto = gerarHTMLCompleto(conteudoHtml, modelo);

            try (OutputStream os = new FileOutputStream(file)) {
                ITextRenderer renderer = new ITextRenderer();
                renderer.setDocumentFromString(htmlCompleto);
                renderer.layout();
                renderer.createPDF(os);
            }

            return file;
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }

    
    private String gerarHTMLCompleto(String conteudoHtml, ModeloCurriculoEnum modelo) {
        String css = obterCSSPorModelo(modelo);
        
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'/>" +
                "<style>" + css + "</style>" +
                "</head>" +
                "<body>" +
                "<div class='curriculo'>" +
                conteudoHtml +
                "</div>" +
                "</body>" +
                "</html>";
    }

    
    private String obterCSSPorModelo(ModeloCurriculoEnum modelo) {
        return switch (modelo) {
            case PROFISSIONAL -> getCSSProfissional();
            case CRIATIVO -> getCSSCriativo();
            case EXECUTIVO -> getCSSExecutivo();
            case ACADEMICO -> getCSSAcademico();
            default -> getCSSProfissional();
        };
    }

    private String getCSSProfissional() {
        return """
            @page {
                size: A4;
                margin: 2cm;
            }
            body {
                font-family: 'Helvetica', Arial, sans-serif;
                font-size: 11pt;
                line-height: 1.6;
                color: #333;
            }
            .curriculo {
                max-width: 100%;
            }
            h1 {
                font-size: 24pt;
                font-weight: bold;
                margin: 0 0 10pt 0;
                color: #2c3e50;
                border-bottom: 2pt solid #3498db;
                padding-bottom: 5pt;
            }
            h2 {
                font-size: 16pt;
                font-weight: bold;
                margin: 15pt 0 8pt 0;
                color: #2c3e50;
            }
            h3 {
                font-size: 13pt;
                font-weight: bold;
                margin: 10pt 0 5pt 0;
                color: #34495e;
            }
            p {
                margin: 0 0 8pt 0;
                text-align: justify;
            }
            ul, ol {
                margin: 5pt 0 10pt 20pt;
                padding: 0;
            }
            li {
                margin-bottom: 3pt;
            }
            strong, b {
                font-weight: bold;
                color: #2c3e50;
            }
            em, i {
                font-style: italic;
            }
            """;
    }

    private String getCSSCriativo() {
        return """
            @page {
                size: A4;
                margin: 1.5cm;
            }
            body {
                font-family: 'Helvetica', Arial, sans-serif;
                font-size: 10pt;
                line-height: 1.5;
                color: #2c3e50;
            }
            .curriculo {
                max-width: 100%;
            }
            h1 {
                font-size: 26pt;
                font-weight: bold;
                margin: 0 0 12pt 0;
                color: #e74c3c;
                text-transform: uppercase;
                letter-spacing: 1pt;
            }
            h2 {
                font-size: 15pt;
                font-weight: bold;
                margin: 12pt 0 6pt 0;
                color: #3498db;
                border-left: 4pt solid #3498db;
                padding-left: 8pt;
            }
            h3 {
                font-size: 12pt;
                font-weight: bold;
                margin: 8pt 0 4pt 0;
                color: #2c3e50;
            }
            p {
                margin: 0 0 6pt 0;
            }
            ul, ol {
                margin: 4pt 0 8pt 15pt;
                padding: 0;
            }
            li {
                margin-bottom: 2pt;
            }
            strong, b {
                font-weight: bold;
                color: #e74c3c;
            }
            """;
    }

    private String getCSSExecutivo() {
        return """
            @page {
                size: A4;
                margin: 2.5cm;
            }
            body {
                font-family: 'Times New Roman', Times, serif;
                font-size: 11pt;
                line-height: 1.7;
                color: #1a1a1a;
            }
            .curriculo {
                max-width: 100%;
            }
            h1 {
                font-size: 28pt;
                font-weight: bold;
                margin: 0 0 15pt 0;
                color: #1a1a1a;
                text-align: center;
                border-top: 3pt solid #1a1a1a;
                border-bottom: 3pt solid #1a1a1a;
                padding: 10pt 0;
            }
            h2 {
                font-size: 16pt;
                font-weight: bold;
                margin: 18pt 0 10pt 0;
                color: #1a1a1a;
                text-transform: uppercase;
                letter-spacing: 0.5pt;
            }
            h3 {
                font-size: 13pt;
                font-weight: bold;
                margin: 12pt 0 6pt 0;
                color: #333;
            }
            p {
                margin: 0 0 10pt 0;
                text-align: justify;
            }
            ul, ol {
                margin: 6pt 0 12pt 25pt;
                padding: 0;
            }
            li {
                margin-bottom: 4pt;
            }
            strong, b {
                font-weight: bold;
            }
            """;
    }

    private String getCSSAcademico() {
        return """
            @page {
                size: A4;
                margin: 3cm;
            }
            body {
                font-family: 'Times New Roman', Times, serif;
                font-size: 12pt;
                line-height: 2.0;
                color: #000;
            }
            .curriculo {
                max-width: 100%;
            }
            h1 {
                font-size: 18pt;
                font-weight: bold;
                margin: 0 0 12pt 0;
                color: #000;
                text-align: center;
            }
            h2 {
                font-size: 14pt;
                font-weight: bold;
                margin: 15pt 0 8pt 0;
                color: #000;
            }
            h3 {
                font-size: 12pt;
                font-weight: bold;
                margin: 10pt 0 5pt 0;
                color: #000;
            }
            p {
                margin: 0 0 12pt 0;
                text-align: justify;
                text-indent: 1cm;
            }
            ul, ol {
                margin: 8pt 0 12pt 30pt;
                padding: 0;
            }
            li {
                margin-bottom: 5pt;
            }
            strong, b {
                font-weight: bold;
            }
            """;
    }
}
