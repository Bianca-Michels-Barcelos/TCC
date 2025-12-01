package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.ResetSenha;
import com.barcelos.recrutamento.core.model.StatusResetSenha;
import com.barcelos.recrutamento.core.port.ResetSenhaRepository;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class ResetSenhaService {

    private static final Logger log = LoggerFactory.getLogger(ResetSenhaService.class);

    private final ResetSenhaRepository resetSenhaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    public ResetSenhaService(ResetSenhaRepository resetSenhaRepository,
                            UsuarioRepository usuarioRepository,
                            EmailService emailService,
                            EmailTemplateService emailTemplateService,
                            PasswordEncoder passwordEncoder) {
        this.resetSenhaRepository = resetSenhaRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
        this.passwordEncoder = passwordEncoder;
        this.secureRandom = new SecureRandom();
    }

    
    @Transactional
    public void solicitarReset(String email) {
        log.info("Solicitação de reset de senha para email: {}", email);

        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        log.debug("Usuário encontrado: {}", usuario.getNome());

        var resetPendente = resetSenhaRepository.findByUsuarioIdAndStatus(
                usuario.getId(),
                StatusResetSenha.PENDENTE
        );

        ResetSenha resetSenha;

        if (resetPendente.isPresent()) {

            log.info("Token pendente encontrado. Estendendo validade.");
            resetSenha = resetPendente.get().estenderValidade();
            resetSenha = resetSenhaRepository.save(resetSenha);
        } else {

            String token = gerarToken();
            log.debug("Novo token gerado para reset de senha");

            resetSenha = ResetSenha.criar(usuario.getId(), token);
            resetSenha = resetSenhaRepository.save(resetSenha);
            log.info("Nova solicitação de reset criada: {}", resetSenha.getId());
        }

        try {
            String htmlContent = emailTemplateService.renderResetSenha(
                    usuario.getNome(),
                    resetSenha.getToken()
            );

            log.info("Enviando email de reset de senha para {}", email);
            emailService.sendHtmlEmailAsync(
                    email,
                    "Recuperação de Senha - Sistema de Recrutamento",
                    htmlContent
            );
            log.info("Email de reset enviado com sucesso (async) para {}", email);
        } catch (Exception e) {
            log.error("Erro ao enviar email de reset para {}: {}", email, e.getMessage(), e);

        }
    }

    
    @Transactional(readOnly = true)
    public ResetSenha validarToken(String token) {
        log.info("Validando token de reset de senha");

        var resetSenha = resetSenhaRepository.findByToken(token)
                .orElseThrow(() -> new BusinessRuleViolationException("Token inválido"));

        if (!resetSenha.isValido()) {
            log.warn("Token expirado ou já utilizado");
            throw new BusinessRuleViolationException("Token expirado ou já utilizado");
        }

        log.info("Token válido");
        return resetSenha;
    }

    
    @Transactional
    public void resetarSenha(String token, String novaSenha) {
        log.info("Resetando senha com token");

        var resetSenha = validarToken(token);

        var usuario = usuarioRepository.findById(resetSenha.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        log.debug("Usuário encontrado: {}", usuario.getNome());

        String senhaHash = passwordEncoder.encode(novaSenha);
        var usuarioAtualizado = usuario.comSenhaHash(senhaHash);
        usuarioRepository.save(usuarioAtualizado);

        var resetUsado = resetSenha.marcarComoUsado();
        resetSenhaRepository.save(resetUsado);

        log.info("Senha resetada com sucesso para usuário {}", usuario.getId());
    }

    
    private String gerarToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

