package com.barcelos.recrutamento.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailTemplateServiceTest {

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailTemplateService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:3000");
    }

    @Test
    void deveRenderizarConfirmacaoCadastro() {
        when(templateEngine.process(eq("email/confirmacao-cadastro"), any(Context.class)))
            .thenReturn("<html>Email Confirmação</html>");

        String resultado = service.renderConfirmacaoCadastro("João Silva", "joao@example.com");

        assertThat(resultado).contains("Email Confirmação");
        verify(templateEngine).process(eq("email/confirmacao-cadastro"), any(Context.class));
    }

    @Test
    void deveRenderizarConviteRecrutador() {
        when(templateEngine.process(eq("email/convite-recrutador"), any(Context.class)))
            .thenReturn("<html>Convite</html>");

        String resultado = service.renderConviteRecrutador("João", "Empresa XYZ", "token123");

        assertThat(resultado).isNotNull();
        verify(templateEngine).process(eq("email/convite-recrutador"), any(Context.class));
    }

    @Test
    void deveRenderizarResetSenha() {
        when(templateEngine.process(eq("email/reset-senha"), any(Context.class)))
            .thenReturn("<html>Reset Senha</html>");

        String resultado = service.renderResetSenha("João", "token123");

        assertThat(resultado).isNotNull();
        verify(templateEngine).process(eq("email/reset-senha"), any(Context.class));
    }

    @Test
    void deveRenderizarVagaCancelada() {
        when(templateEngine.process(eq("email/vaga-cancelada"), any(Context.class)))
            .thenReturn("<html>Vaga Cancelada</html>");

        String resultado = service.renderVagaCancelada("João", "Desenvolvedor", "Empresa XYZ");

        assertThat(resultado).isNotNull();
        verify(templateEngine).process(eq("email/vaga-cancelada"), any(Context.class));
    }

    @Test
    void deveRenderizarConviteProcesso() {
        when(templateEngine.process(eq("email/convite-processo"), any(Context.class)))
            .thenReturn("<html>Convite Processo</html>");

        String resultado = service.renderConviteProcesso("João", "Vaga", "Mensagem", "conviteId");

        assertThat(resultado).isNotNull();
        verify(templateEngine).process(eq("email/convite-processo"), any(Context.class));
    }

    @Test
    void deveRenderizarFeedbackCandidato() {
        when(templateEngine.process(eq("email/feedback-candidato"), any(Context.class)))
            .thenReturn("<html>Feedback</html>");

        String resultado = service.renderFeedbackCandidato("João", "Vaga", "Empresa", "Feedback positivo", "POSITIVO");

        assertThat(resultado).isNotNull();
        verify(templateEngine).process(eq("email/feedback-candidato"), any(Context.class));
    }

    @Test
    void deveRenderizarTransferenciaVagas() {
        when(templateEngine.process(eq("email/transferencia-vagas"), any(Context.class)))
            .thenReturn("<html>Transferência</html>");

        List<EmailTemplateService.VagaTransferida> vagas = List.of(
            new EmailTemplateService.VagaTransferida("id1", "Vaga 1", "ABERTA", 5)
        );

        String resultado = service.renderTransferenciaVagas("João", "Maria", 1, vagas);

        assertThat(resultado).isNotNull();
        verify(templateEngine).process(eq("email/transferencia-vagas"), any(Context.class));
    }

    @Test
    void deveRenderizarAlteracaoPapel() {
        when(templateEngine.process(eq("email/alteracao-papel"), any(Context.class)))
            .thenReturn("<html>Alteração Papel</html>");

        String resultado = service.renderAlteracaoPapel("João", "RECRUTADOR", "ADMIN", "Empresa");

        assertThat(resultado).isNotNull();
        verify(templateEngine).process(eq("email/alteracao-papel"), any(Context.class));
    }
}

