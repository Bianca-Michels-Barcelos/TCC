package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.CandidatoCompatibilidadeResponse;
import com.barcelos.recrutamento.api.dto.CompatibilidadeResponse;
import com.barcelos.recrutamento.api.dto.VagaCompatibilidadeResponse;
import com.barcelos.recrutamento.config.OrganizacaoSecurityService;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import com.barcelos.recrutamento.core.port.VagaRepository;
import com.barcelos.recrutamento.core.service.CompatibilidadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/compatibilidade")
public class CompatibilidadeController {

    private final CompatibilidadeService compatibilidadeService;
    private final UsuarioRepository usuarioRepository;
    private final VagaRepository vagaRepository;
    private final OrganizacaoSecurityService orgSecurityService;

    public CompatibilidadeController(
            CompatibilidadeService compatibilidadeService,
            UsuarioRepository usuarioRepository,
            VagaRepository vagaRepository,
            OrganizacaoSecurityService orgSecurityService
    ) {
        this.compatibilidadeService = compatibilidadeService;
        this.usuarioRepository = usuarioRepository;
        this.vagaRepository = vagaRepository;
        this.orgSecurityService = orgSecurityService;
    }

    
    @PreAuthorize("hasAnyRole('CANDIDATO', 'RECRUTADOR', 'ADMIN')")
    @GetMapping("/candidato/{candidatoUsuarioId}/vaga/{vagaId}")
    public ResponseEntity<CompatibilidadeResponse> calcularCompatibilidade(
            @PathVariable UUID candidatoUsuarioId,
            @PathVariable UUID vagaId,
            Authentication authentication
    ) {

        validateAccess(candidatoUsuarioId, vagaId, authentication);

        var compatibilidade = compatibilidadeService.calcularCompatibilidade(candidatoUsuarioId, vagaId);
        return ResponseEntity.ok(compatibilidade);
    }

    private void validateAccess(UUID candidatoUsuarioId, UUID vagaId, Authentication authentication) {
        UUID usuarioIdAutenticado = getUsuarioId(authentication);

        if (usuarioIdAutenticado.equals(candidatoUsuarioId)) {
            return;
        }

        var vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga", vagaId));

        try {
            orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);
        } catch (Exception e) {
            throw new RuntimeException("Você não tem permissão para acessar esta compatibilidade");
        }
    }

    private UUID getUsuarioId(Authentication authentication) {
        String email = authentication.getName();
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", email));
        return usuario.getId();
    }
}
