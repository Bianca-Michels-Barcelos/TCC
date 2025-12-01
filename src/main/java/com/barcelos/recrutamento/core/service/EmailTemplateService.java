package com.barcelos.recrutamento.core.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class EmailTemplateService {

    private final TemplateEngine templateEngine;
    private final String baseUrl;

    public EmailTemplateService(
            TemplateEngine templateEngine,
            @Value("${app.email.base-url}") String baseUrl
    ) {
        this.templateEngine = templateEngine;
        this.baseUrl = baseUrl;
    }

    public String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariable("baseUrl", baseUrl);

        if (variables != null) {
            variables.forEach(context::setVariable);
        }

        return templateEngine.process(templateName, context);
    }

    public String renderConfirmacaoCadastro(String nomeCandidato, String emailCandidato) {
        return renderTemplate("email/confirmacao-cadastro", Map.of(
                "nomeCandidato", nomeCandidato,
                "emailCandidato", emailCandidato
        ));
    }

    public String renderConviteRecrutador(String nomeRecrutador, String nomeOrganizacao, String token) {
        return renderTemplate("email/convite-recrutador", Map.of(
                "nomeRecrutador", nomeRecrutador,
                "nomeOrganizacao", nomeOrganizacao,
                "linkConvite", baseUrl + "/auth/convite/" + token
        ));
    }

    
    public String renderVagaCancelada(String nomeCandidato, String tituloVaga, String nomeOrganizacao) {
        return renderTemplate("email/vaga-cancelada", Map.of(
                "nomeCandidato", nomeCandidato,
                "tituloVaga", tituloVaga,
                "nomeOrganizacao", nomeOrganizacao
        ));
    }

    
    public String renderConviteProcesso(String nomeCandidato, String tituloVaga,
                                       String mensagemRecrutador, String conviteId) {
        return renderTemplate("email/convite-processo", Map.of(
                "nomeCandidato", nomeCandidato,
                "tituloVaga", tituloVaga,
                "mensagemRecrutador", mensagemRecrutador != null ? mensagemRecrutador : "",
                "linkConvite", baseUrl + "/dashboard/minhas-candidaturas"
        ));
    }

    
    public String renderResetSenha(String nomeUsuario, String token) {
        return renderTemplate("email/reset-senha", Map.of(
                "nomeUsuario", nomeUsuario,
                "linkReset", baseUrl + "/redefinir-senha?token=" + token
        ));
    }

    public String renderFeedbackCandidato(String nomeCandidato, String tituloVaga,
                                         String nomeOrganizacao, String feedback, String tipoFeedback) {
        return renderTemplate("email/feedback-candidato", Map.of(
                "nomeCandidato", nomeCandidato,
                "tituloVaga", tituloVaga,
                "nomeOrganizacao", nomeOrganizacao,
                "feedback", feedback != null ? feedback : "",
                "tipoFeedback", tipoFeedback
        ));
    }

    public String renderTransferenciaVagas(String nomeRecrutador, String nomeRecrutadorOrigem, 
                                          int quantidadeVagas, java.util.List<VagaTransferida> vagas) {
        return renderTemplate("email/transferencia-vagas", Map.of(
                "nomeRecrutador", nomeRecrutador,
                "nomeRecrutadorOrigem", nomeRecrutadorOrigem,
                "quantidadeVagas", quantidadeVagas,
                "vagas", vagas
        ));
    }

    public String renderAlteracaoPapel(String nomeRecrutador, String papelAnterior, 
                                      String novoPapel, String nomeOrganizacao) {
        return renderTemplate("email/alteracao-papel", Map.of(
                "nomeRecrutador", nomeRecrutador,
                "papelAnterior", papelAnterior,
                "novoPapel", novoPapel,
                "nomeOrganizacao", nomeOrganizacao
        ));
    }

    public record VagaTransferida(String id, String titulo, String status, int quantidadeCandidatos) {
    }
}
