package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.ConviteRecrutador;
import com.barcelos.recrutamento.core.model.StatusConvite;
import com.barcelos.recrutamento.core.port.ConviteRecrutadorRepository;
import com.barcelos.recrutamento.core.port.OrganizacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
public class ConviteRecrutadorService {

    private static final Logger log = LoggerFactory.getLogger(ConviteRecrutadorService.class);

    private final ConviteRecrutadorRepository conviteRepository;
    private final OrganizacaoRepository organizacaoRepository;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final SecureRandom secureRandom;

    public ConviteRecrutadorService(ConviteRecrutadorRepository conviteRepository,
                                   OrganizacaoRepository organizacaoRepository,
                                   EmailService emailService,
                                   EmailTemplateService emailTemplateService) {
        this.conviteRepository = conviteRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
        this.secureRandom = new SecureRandom();
    }

    
    @Transactional
    public ConviteRecrutador criarConvite(UUID organizacaoId, String email) {
        log.info("Criando convite para email {} na organização {}", email, organizacaoId);

        var organizacao = organizacaoRepository.findById(organizacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", organizacaoId));

        log.debug("Organização encontrada: {}", organizacao.getNome());

        if (conviteRepository.existsByEmailAndOrganizacaoAndStatus(email, organizacaoId, StatusConvite.PENDENTE)) {
            log.warn("Já existe convite pendente para email {} na organização {}", email, organizacaoId);
            throw new BusinessRuleViolationException("Já existe um convite pendente para este e-mail");
        }

        String token = gerarToken();
        log.debug("Token gerado para convite");

        var convite = ConviteRecrutador.criar(organizacaoId, email, token);
        convite = conviteRepository.save(convite);
        log.info("Convite {} criado com sucesso", convite.getId());

        try {
            String htmlContent = emailTemplateService.renderConviteRecrutador(
                    email,
                    organizacao.getNome(),
                    token
            );

            log.info("Enviando email de convite para {}", email);
            emailService.sendHtmlEmailAsync(
                    email,
                    "Convite para se juntar à " + organizacao.getNome(),
                    htmlContent
            );
            log.info("Email de convite enviado com sucesso (async) para {}", email);
        } catch (Exception e) {
            log.error("Erro ao enviar email de convite para {}: {}", email, e.getMessage(), e);

        }

        return convite;
    }

    
    @Transactional(readOnly = true)
    public ConviteRecrutador buscarPorToken(String token) {
        return conviteRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Convite", token));
    }

    
    @Transactional
    public ConviteRecrutador aceitarConvite(String token) {
        var convite = buscarPorToken(token);

        if (!convite.isValido()) {
            throw new BusinessRuleViolationException("Convite inválido ou expirado");
        }

        var conviteAceito = convite.aceitar();
        return conviteRepository.save(conviteAceito);
    }

    
    @Transactional
    public ConviteRecrutador recusarConvite(String token) {
        var convite = buscarPorToken(token);

        if (convite.getStatus() != StatusConvite.PENDENTE) {
            throw new BusinessRuleViolationException("Convite não está pendente");
        }

        var conviteRecusado = convite.recusar();
        return conviteRepository.save(conviteRecusado);
    }

    
    private String gerarToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
