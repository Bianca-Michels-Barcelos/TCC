package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.ConviteRecrutadorResponse;
import com.barcelos.recrutamento.core.service.ConviteRecrutadorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/convite")
public class AuthConviteController {

    private static final Logger log = LoggerFactory.getLogger(AuthConviteController.class);

    private final ConviteRecrutadorService conviteService;

    public AuthConviteController(ConviteRecrutadorService conviteService) {
        this.conviteService = conviteService;
    }

    
    @GetMapping("/{token}")
    public ResponseEntity<ConviteRecrutadorResponse> buscarConvite(@PathVariable String token) {
        log.info("Buscando convite com token: {}", token);
        
        try {
            var convite = conviteService.buscarPorToken(token);
            log.info("Convite encontrado: ID={}, Email={}, Status={}", 
                    convite.getId(), convite.getEmail(), convite.getStatus());

            var response = new ConviteRecrutadorResponse(
                    convite.getId(),
                    convite.getOrganizacaoId(),
                    convite.getEmail(),
                    convite.getStatus().name(),
                    convite.getDataEnvio(),
                    convite.getDataExpiracao(),
                    convite.getDataAceite()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erro ao buscar convite com token {}: {}", token, e.getMessage(), e);
            throw e;
        }
    }

    
    @PostMapping("/{token}/aceitar")
    public ResponseEntity<ConviteRecrutadorResponse> aceitarConvite(@PathVariable String token) {
        var convite = conviteService.aceitarConvite(token);

        var response = new ConviteRecrutadorResponse(
                convite.getId(),
                convite.getOrganizacaoId(),
                convite.getEmail(),
                convite.getStatus().name(),
                convite.getDataEnvio(),
                convite.getDataExpiracao(),
                convite.getDataAceite()
        );

        return ResponseEntity.ok(response);
    }

    
    @PostMapping("/{token}/recusar")
    public ResponseEntity<Void> recusarConvite(@PathVariable String token) {
        conviteService.recusarConvite(token);
        return ResponseEntity.noContent().build();
    }
}
