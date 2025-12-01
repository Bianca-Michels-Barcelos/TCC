package com.barcelos.recrutamento.core.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "fromEmail", "noreply@example.com");
    }

    @Test
    void deveEnviarEmailComSucesso() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        service.sendHtmlEmail("dest@example.com", "Assunto Teste", "<html><body>Conteúdo</body></html>");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void deveEnviarEmailAssincronoComSucesso() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        service.sendHtmlEmailAsync("dest@example.com", "Assunto", "<html>Conteúdo</html>");

        verify(mailSender, timeout(1000)).send(any(MimeMessage.class));
    }

    @Test
    void deveTratarErroAoEnviarEmail() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> service.sendHtmlEmail("dest@example.com", "Assunto", "Conteúdo"))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void deveUsarFromEmailConfigurado() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        service.sendHtmlEmail("dest@example.com", "Assunto", "<html>Teste</html>");

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }
}

