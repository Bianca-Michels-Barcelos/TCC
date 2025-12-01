package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.ResetSenhaRequest;
import com.barcelos.recrutamento.api.dto.SolicitarResetSenhaRequest;
import com.barcelos.recrutamento.api.dto.ValidarTokenResetResponse;
import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import com.barcelos.recrutamento.core.service.ResetSenhaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/reset-senha")
public class ResetSenhaPublicController {

    private static final Logger log = LoggerFactory.getLogger(ResetSenhaPublicController.class);

    private final ResetSenhaService resetSenhaService;
    private final UsuarioRepository usuarioRepository;

    public ResetSenhaPublicController(ResetSenhaService resetSenhaService,
                                     UsuarioRepository usuarioRepository) {
        this.resetSenhaService = resetSenhaService;
        this.usuarioRepository = usuarioRepository;
    }

    
    @PostMapping("/solicitar")
    public ResponseEntity<Void> solicitarReset(@Valid @RequestBody SolicitarResetSenhaRequest request) {
        log.info("Solicitação de reset de senha recebida");

        try {
            resetSenhaService.solicitarReset(request.email());
        } catch (Exception e) {

            log.warn("Erro ao solicitar reset de senha: {}", e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    
    @GetMapping("/validar/{token}")
    public ResponseEntity<ValidarTokenResetResponse> validarToken(@PathVariable String token) {
        log.info("Validação de token de reset");

        try {
            var resetSenha = resetSenhaService.validarToken(token);
            var usuario = usuarioRepository.findById(resetSenha.getUsuarioId())
                    .orElseThrow(() -> new BusinessRuleViolationException("Usuário não encontrado"));

            return ResponseEntity.ok(new ValidarTokenResetResponse(
                    true,
                    usuario.getEmail().value()
            ));
        } catch (Exception e) {
            log.warn("Token inválido: {}", e.getMessage());
            return ResponseEntity.ok(new ValidarTokenResetResponse(false, null));
        }
    }

    
    @PostMapping("/confirmar")
    public ResponseEntity<Void> confirmarReset(@Valid @RequestBody ResetSenhaRequest request) {
        log.info("Confirmação de reset de senha");

        resetSenhaService.resetarSenha(request.token(), request.novaSenha());

        return ResponseEntity.ok().build();
    }
}

