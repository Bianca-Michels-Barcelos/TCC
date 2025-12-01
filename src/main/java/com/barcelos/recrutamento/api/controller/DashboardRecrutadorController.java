package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.dashboard.*;
import com.barcelos.recrutamento.config.OrganizacaoSecurityService;
import com.barcelos.recrutamento.core.service.DashboardRecrutadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard/recrutador")
public class DashboardRecrutadorController {

    private final DashboardRecrutadorService dashboardService;
    private final OrganizacaoSecurityService orgSecurityService;
    private final com.barcelos.recrutamento.config.SecurityHelper securityHelper;

    public DashboardRecrutadorController(
            DashboardRecrutadorService dashboardService,
            OrganizacaoSecurityService orgSecurityService,
            com.barcelos.recrutamento.config.SecurityHelper securityHelper
    ) {
        this.dashboardService = dashboardService;
        this.orgSecurityService = orgSecurityService;
        this.securityHelper = securityHelper;
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/{organizacaoId}")
    public ResponseEntity<DashboardRecrutadorResponse> getDashboard(
            @PathVariable UUID organizacaoId,
            Authentication authentication
    ) {
        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);
        UUID recrutadorUsuarioId = securityHelper.getUserIdFromAuthentication(authentication);
        var dashboard = dashboardService.gerarDashboard(recrutadorUsuarioId);
        return ResponseEntity.ok(dashboard);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/{organizacaoId}/atividades-recentes")
    public ResponseEntity<List<AtividadeRecenteResponse>> getAtividadesRecentes(
            @PathVariable UUID organizacaoId,
            @RequestParam(defaultValue = "10") int limite,
            Authentication authentication
    ) {
        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);
        UUID recrutadorUsuarioId = securityHelper.getUserIdFromAuthentication(authentication);
        var atividades = dashboardService.buscarAtividadesRecentes(recrutadorUsuarioId, limite);
        return ResponseEntity.ok(atividades);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/{organizacaoId}/vagas-atencao")
    public ResponseEntity<List<VagaAtencaoResponse>> getVagasAtencao(
            @PathVariable UUID organizacaoId,
            Authentication authentication
    ) {
        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);
        UUID recrutadorUsuarioId = securityHelper.getUserIdFromAuthentication(authentication);
        var vagasAtencao = dashboardService.buscarVagasAtencao(recrutadorUsuarioId);
        return ResponseEntity.ok(vagasAtencao);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/{organizacaoId}/entrevistas-proximas")
    public ResponseEntity<List<EntrevistaProximaResponse>> getEntrevistasProximas(
            @PathVariable UUID organizacaoId,
            Authentication authentication
    ) {
        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);
        UUID recrutadorUsuarioId = securityHelper.getUserIdFromAuthentication(authentication);
        var entrevistas = dashboardService.buscarEntrevistasProximas(recrutadorUsuarioId);
        return ResponseEntity.ok(entrevistas);
    }
}
