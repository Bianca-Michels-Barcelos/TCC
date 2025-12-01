package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.AdicionarNivelExperienciaRequest;
import com.barcelos.recrutamento.config.OrganizacaoSecurityService;
import com.barcelos.recrutamento.core.exception.ResourceOwnershipException;
import com.barcelos.recrutamento.core.model.NivelExperiencia;
import com.barcelos.recrutamento.core.service.NivelExperienciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/organizacoes/{organizacaoId}/niveis-experiencia")
public class NivelExperienciaController {

    private final NivelExperienciaService nivelExperienciaService;
    private final OrganizacaoSecurityService orgSecurityService;

    public NivelExperienciaController(NivelExperienciaService nivelExperienciaService,
                                     OrganizacaoSecurityService orgSecurityService) {
        this.nivelExperienciaService = nivelExperienciaService;
        this.orgSecurityService = orgSecurityService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PostMapping
    public ResponseEntity<NivelExperiencia> criar(
            @PathVariable UUID organizacaoId,
            @Valid @RequestBody AdicionarNivelExperienciaRequest request,
            Authentication authentication) {
        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);

        var nivelExperiencia = nivelExperienciaService.criar(organizacaoId, request.descricao());
        return ResponseEntity.ok(nivelExperiencia);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/{nivelExperienciaId}")
    public ResponseEntity<NivelExperiencia> buscar(
            @PathVariable UUID organizacaoId,
            @PathVariable UUID nivelExperienciaId,
            Authentication authentication) {
        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);

        var nivelExperiencia = nivelExperienciaService.buscar(nivelExperienciaId);

        if (!nivelExperiencia.getOrganizacaoId().equals(organizacaoId)) {
            throw new ResourceOwnershipException("Nível de experiência não pertence a esta organização");
        }

        return ResponseEntity.ok(nivelExperiencia);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping
    public ResponseEntity<List<NivelExperiencia>> listar(
            @PathVariable UUID organizacaoId,
            Authentication authentication) {
        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);

        var niveisExperiencia = nivelExperienciaService.listarPorOrganizacao(organizacaoId);
        return ResponseEntity.ok(niveisExperiencia);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PutMapping("/{nivelExperienciaId}")
    public ResponseEntity<NivelExperiencia> atualizar(
            @PathVariable UUID organizacaoId,
            @PathVariable UUID nivelExperienciaId,
            @Valid @RequestBody AdicionarNivelExperienciaRequest request,
            Authentication authentication) {
        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);

        var nivelExperiencia = nivelExperienciaService.buscar(nivelExperienciaId);

        if (!nivelExperiencia.getOrganizacaoId().equals(organizacaoId)) {
            throw new ResourceOwnershipException("Nível de experiência não pertence a esta organização");
        }

        var nivelExperienciaAtualizado = nivelExperienciaService.atualizar(nivelExperiencia, request.descricao());
        return ResponseEntity.ok(nivelExperienciaAtualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{nivelExperienciaId}")
    public ResponseEntity<Void> deletar(
            @PathVariable UUID organizacaoId,
            @PathVariable UUID nivelExperienciaId,
            Authentication authentication) {
        orgSecurityService.validateUserIsAdminOfOrganization(organizacaoId, authentication);

        var nivelExperiencia = nivelExperienciaService.buscar(nivelExperienciaId);

        if (!nivelExperiencia.getOrganizacaoId().equals(organizacaoId)) {
            throw new ResourceOwnershipException("Nível de experiência não pertence a esta organização");
        }

        nivelExperienciaService.deletar(nivelExperienciaId);
        return ResponseEntity.noContent().build();
    }
}
