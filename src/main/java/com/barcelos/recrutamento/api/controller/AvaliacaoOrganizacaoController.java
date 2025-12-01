package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.AtualizarAvaliacaoOrganizacaoRequest;
import com.barcelos.recrutamento.api.dto.CriarAvaliacaoOrganizacaoRequest;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.AvaliacaoOrganizacao;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import com.barcelos.recrutamento.core.service.AvaliacaoOrganizacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/avaliacoes-organizacao")
public class AvaliacaoOrganizacaoController {

    private final AvaliacaoOrganizacaoService service;
    private final UsuarioRepository usuarioRepository;

    public AvaliacaoOrganizacaoController(AvaliacaoOrganizacaoService service,
                                         UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @PostMapping
    public ResponseEntity<AvaliacaoOrganizacao> criar(
            @Valid @RequestBody CriarAvaliacaoOrganizacaoRequest request,
            Authentication authentication
    ) {
        UUID candidatoUsuarioId = getUsuarioId(authentication);

        var avaliacao = service.criar(
            request.processoId(),
            candidatoUsuarioId,
            request.nota(),
            request.comentario()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(avaliacao);
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @PutMapping("/{avaliacaoId}")
    public ResponseEntity<AvaliacaoOrganizacao> atualizar(
            @PathVariable UUID avaliacaoId,
            @Valid @RequestBody AtualizarAvaliacaoOrganizacaoRequest request,
            Authentication authentication
    ) {
        UUID candidatoUsuarioId = getUsuarioId(authentication);

        var avaliacao = service.atualizar(
            avaliacaoId,
            candidatoUsuarioId,
            request.nota(),
            request.comentario()
        );

        return ResponseEntity.ok(avaliacao);
    }

    
    @PreAuthorize("hasAnyRole('CANDIDATO', 'ADMIN', 'RECRUTADOR')")
    @GetMapping("/processo/{processoId}")
    public ResponseEntity<AvaliacaoOrganizacao> buscarPorProcesso(@PathVariable UUID processoId) {
        return service.buscarPorProcesso(processoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    
    @GetMapping("/organizacao/{organizacaoId}")
    public ResponseEntity<List<AvaliacaoOrganizacao>> listarPorOrganizacao(@PathVariable UUID organizacaoId) {
        var avaliacoes = service.listarPorOrganizacao(organizacaoId);
        return ResponseEntity.ok(avaliacoes);
    }

    
    @GetMapping("/organizacao/{organizacaoId}/estatisticas")
    public ResponseEntity<Map<String, Object>> buscarEstatisticas(@PathVariable UUID organizacaoId) {
        Double notaMedia = service.calcularNotaMedia(organizacaoId);
        long totalAvaliacoes = service.contarAvaliacoes(organizacaoId);

        Map<String, Object> estatisticas = new HashMap<>();
        estatisticas.put("notaMedia", notaMedia);
        estatisticas.put("totalAvaliacoes", totalAvaliacoes);

        return ResponseEntity.ok(estatisticas);
    }

    private UUID getUsuarioId(Authentication authentication) {
        String email = authentication.getName();
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", email));
        return usuario.getId();
    }
}
