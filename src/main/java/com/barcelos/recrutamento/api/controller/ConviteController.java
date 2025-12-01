package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.ConviteRecrutadorRequest;
import com.barcelos.recrutamento.api.dto.ConviteRecrutadorResponse;
import com.barcelos.recrutamento.config.OrganizacaoSecurityService;
import com.barcelos.recrutamento.core.service.ConviteRecrutadorService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/organizacoes/{organizacaoId}/convites")
public class ConviteController {

    private final ConviteRecrutadorService conviteService;
    private final OrganizacaoSecurityService orgSecurityService;

    public ConviteController(ConviteRecrutadorService conviteService,
                            OrganizacaoSecurityService orgSecurityService) {
        this.conviteService = conviteService;
        this.orgSecurityService = orgSecurityService;
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PostMapping
    public ResponseEntity<ConviteRecrutadorResponse> criarConvite(
            @PathVariable UUID organizacaoId,
            @Valid @RequestBody ConviteRecrutadorRequest request,
            Authentication authentication
    ) {
        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);

        var convite = conviteService.criarConvite(organizacaoId, request.email());

        var response = new ConviteRecrutadorResponse(
                convite.getId(),
                convite.getOrganizacaoId(),
                convite.getEmail(),
                convite.getStatus().name(),
                convite.getDataEnvio(),
                convite.getDataExpiracao(),
                convite.getDataAceite()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
