package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.AdicionarBeneficioOrgRequest;
import com.barcelos.recrutamento.config.OrganizacaoSecurityService;
import com.barcelos.recrutamento.core.exception.ResourceOwnershipException;
import com.barcelos.recrutamento.core.model.BeneficioOrg;
import com.barcelos.recrutamento.core.service.BeneficioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/organizacoes/{organizacaoId}/beneficios")
public class BeneficioController {

    private final BeneficioService beneficioService;
    private final OrganizacaoSecurityService orgSecurityService;

    public BeneficioController(BeneficioService beneficioService,
                              OrganizacaoSecurityService orgSecurityService) {
        this.beneficioService = beneficioService;
        this.orgSecurityService = orgSecurityService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PostMapping
    public ResponseEntity<BeneficioOrg> criar(
            @PathVariable UUID organizacaoId,
            @Valid @RequestBody AdicionarBeneficioOrgRequest request,
            Authentication authentication) {
        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);

        var beneficio = beneficioService.criar(organizacaoId, request.nome(), request.descricao());
        return ResponseEntity.ok(beneficio);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/{beneficioId}")
    public ResponseEntity<BeneficioOrg> buscar(
            @PathVariable UUID organizacaoId,
            @PathVariable UUID beneficioId,
            Authentication authentication) {
        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);

        var beneficio = beneficioService.buscar(beneficioId);

        if (!beneficio.getOrganizacaoId().equals(organizacaoId)) {
            throw new ResourceOwnershipException("Benefício não pertence a esta organização");
        }

        return ResponseEntity.ok(beneficio);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping
    public ResponseEntity<List<BeneficioOrg>> listar(
            @PathVariable UUID organizacaoId,
            Authentication authentication) {
        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);

        var beneficios = beneficioService.listarPorOrganizacao(organizacaoId);
        return ResponseEntity.ok(beneficios);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PutMapping("/{beneficioId}")
    public ResponseEntity<BeneficioOrg> atualizar(
            @PathVariable UUID organizacaoId,
            @PathVariable UUID beneficioId,
            @Valid @RequestBody AdicionarBeneficioOrgRequest request,
            Authentication authentication) {
        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);

        var beneficio = beneficioService.buscar(beneficioId);

        if (!beneficio.getOrganizacaoId().equals(organizacaoId)) {
            throw new ResourceOwnershipException("Benefício não pertence a esta organização");
        }

        var beneficioAtualizado = beneficioService.atualizar(beneficio, request.nome(), request.descricao());
        return ResponseEntity.ok(beneficioAtualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{beneficioId}")
    public ResponseEntity<Void> deletar(
            @PathVariable UUID organizacaoId,
            @PathVariable UUID beneficioId,
            Authentication authentication) {
        orgSecurityService.validateUserIsAdminOfOrganization(organizacaoId, authentication);

        var beneficio = beneficioService.buscar(beneficioId);

        if (!beneficio.getOrganizacaoId().equals(organizacaoId)) {
            throw new ResourceOwnershipException("Benefício não pertence a esta organização");
        }

        beneficioService.deletar(beneficioId);
        return ResponseEntity.noContent().build();
    }
}
