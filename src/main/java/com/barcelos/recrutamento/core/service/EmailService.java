package com.barcelos.recrutamento.core.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.email.from}") String fromEmail
    ) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        log.info("Preparando email para {}", to);
        log.debug("From: {}, Subject: {}", fromEmail, subject);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        log.info("Enviando email para {}...", to);
        mailSender.send(message);
        log.info("Email enviado com sucesso para {}", to);
    }

    
    @Async
    public void sendHtmlEmailAsync(String to, String subject, String htmlContent) {
        log.info("Enviando email assíncrono para {}", to);
        try {
            sendHtmlEmail(to, subject, htmlContent);
        } catch (MessagingException e) {
            log.error("Erro ao enviar e-mail assíncrono para {}: {}", to, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro inesperado ao enviar e-mail para {}: {}", to, e.getMessage(), e);
        }
    }
}
