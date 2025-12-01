package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.dashboard.*;
import com.barcelos.recrutamento.config.SecurityHelper;
import com.barcelos.recrutamento.core.service.DashboardCandidatoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard/candidato")
public class DashboardCandidatoController {

    private final DashboardCandidatoService dashboardService;
    private final SecurityHelper securityHelper;

    public DashboardCandidatoController(
            DashboardCandidatoService dashboardService,
            SecurityHelper securityHelper
    ) {
        this.dashboardService = dashboardService;
        this.securityHelper = securityHelper;
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @GetMapping
    public ResponseEntity<DashboardCandidatoResponse> getDashboard(Authentication authentication) {
        var usuarioId = securityHelper.getUserIdFromAuthentication(authentication);
        var dashboard = dashboardService.gerarDashboard(usuarioId);
        return ResponseEntity.ok(dashboard);
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @GetMapping("/atualizacoes-recentes")
    public ResponseEntity<List<AtualizacaoRecenteResponse>> getAtualizacoesRecentes(
            @RequestParam(defaultValue = "10") int limite,
            Authentication authentication
    ) {
        var usuarioId = securityHelper.getUserIdFromAuthentication(authentication);
        var atualizacoes = dashboardService.buscarAtualizacoesRecentes(usuarioId, limite);
        return ResponseEntity.ok(atualizacoes);
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @GetMapping("/proximas-etapas")
    public ResponseEntity<List<ProximaEtapaResponse>> getProximasEtapas(Authentication authentication) {
        var usuarioId = securityHelper.getUserIdFromAuthentication(authentication);
        var proximasEtapas = dashboardService.buscarProximasEtapas(usuarioId);
        return ResponseEntity.ok(proximasEtapas);
    }
}
