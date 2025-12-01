package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.AtualizarVagaExternaRequest;
import com.barcelos.recrutamento.api.dto.CriarVagaExternaRequest;
import com.barcelos.recrutamento.api.dto.VagaExternaResponse;
import com.barcelos.recrutamento.config.SecurityHelper;
import com.barcelos.recrutamento.core.model.ModeloCurriculoEnum;
import com.barcelos.recrutamento.core.model.VagaExterna;
import com.barcelos.recrutamento.core.service.VagaExternaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/vagas-externas")
public class VagaExternaController {

    private final VagaExternaService service;
    private final SecurityHelper securityHelper;

    public VagaExternaController(VagaExternaService service, SecurityHelper securityHelper) {
        this.service = service;
        this.securityHelper = securityHelper;
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @PostMapping
    public ResponseEntity<VagaExternaResponse> criar(
            @RequestBody @Valid CriarVagaExternaRequest request,
            Authentication authentication) {

        UUID candidatoUsuarioId = securityHelper.getUserIdFromAuthentication(authentication);

        if (request.candidatoUsuarioId() != null && !request.candidatoUsuarioId().equals(candidatoUsuarioId)) {
            throw new AccessDeniedException("Você só pode criar vagas para si mesmo");
        }

        VagaExterna vagaExterna = service.criar(
                request.titulo(),
                request.descricao(),
                request.requisitos(),
                candidatoUsuarioId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(VagaExternaResponse.fromDomain(vagaExterna));
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @GetMapping("/{id}")
    public ResponseEntity<VagaExternaResponse> buscar(
            @PathVariable UUID id,
            Authentication authentication) {
        VagaExterna vagaExterna = service.buscar(id);

        validateOwnership(vagaExterna, authentication);

        return ResponseEntity.ok(VagaExternaResponse.fromDomain(vagaExterna));
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @GetMapping
    public ResponseEntity<List<VagaExternaResponse>> listarPorUsuario(Authentication authentication) {

        UUID usuarioId = securityHelper.getUserIdFromAuthentication(authentication);

        List<VagaExterna> vagas = service.listarPorUsuario(usuarioId);
        List<VagaExternaResponse> responses = vagas.stream()
                .map(VagaExternaResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(responses);
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @PutMapping("/{id}")
    public ResponseEntity<VagaExternaResponse> atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid AtualizarVagaExternaRequest request,
            Authentication authentication) {

        VagaExterna vagaExistente = service.buscar(id);
        validateOwnership(vagaExistente, authentication);

        VagaExterna vagaExterna = service.atualizar(
                id,
                request.titulo(),
                request.descricao(),
                request.requisitos()
        );
        return ResponseEntity.ok(VagaExternaResponse.fromDomain(vagaExterna));
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @PostMapping("/{id}/curriculo")
    public ResponseEntity<VagaExternaResponse> gerarCurriculo(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        VagaExterna vagaExistente = service.buscar(id);
        validateOwnership(vagaExistente, authentication);

        String modeloStr = request.get("modelo");
        ModeloCurriculoEnum modelo = modeloStr != null
            ? ModeloCurriculoEnum.valueOf(modeloStr)
            : ModeloCurriculoEnum.PROFISSIONAL;

        VagaExterna vagaAtualizada = service.gerarCurriculoComIA(id, modelo);
        return ResponseEntity.ok(VagaExternaResponse.fromDomain(vagaAtualizada));
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @GetMapping("/{id}/curriculo/conteudo")
    public ResponseEntity<Map<String, Object>> obterConteudoCurriculo(
            @PathVariable UUID id,
            Authentication authentication) {
        VagaExterna vagaExistente = service.buscar(id);
        validateOwnership(vagaExistente, authentication);

        if (vagaExistente.getConteudoCurriculo() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
            "conteudo", vagaExistente.getConteudoCurriculo(),
            "modelo", vagaExistente.getModeloCurriculo() != null
                ? vagaExistente.getModeloCurriculo().name()
                : ModeloCurriculoEnum.PROFISSIONAL.name()
        ));
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @PutMapping("/{id}/curriculo")
    public ResponseEntity<VagaExternaResponse> atualizarCurriculo(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        VagaExterna vagaExistente = service.buscar(id);
        validateOwnership(vagaExistente, authentication);

        String novoConteudo = request.get("conteudo");
        if (novoConteudo == null || novoConteudo.isBlank()) {
            throw new IllegalArgumentException("Conteúdo do currículo não pode ser vazio");
        }

        VagaExterna vagaAtualizada = service.atualizarCurriculo(id, novoConteudo);
        return ResponseEntity.ok(VagaExternaResponse.fromDomain(vagaAtualizada));
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @PostMapping("/{id}/curriculo/regenerar")
    public ResponseEntity<VagaExternaResponse> regenerarCurriculoComNovoModelo(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        VagaExterna vagaExistente = service.buscar(id);
        validateOwnership(vagaExistente, authentication);

        String modeloStr = request.get("modelo");
        ModeloCurriculoEnum novoModelo = modeloStr != null
            ? ModeloCurriculoEnum.valueOf(modeloStr)
            : ModeloCurriculoEnum.PROFISSIONAL;

        VagaExterna vagaAtualizada = service.regenerarCurriculoComModelo(id, novoModelo);
        return ResponseEntity.ok(VagaExternaResponse.fromDomain(vagaAtualizada));
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<VagaExternaResponse> desativar(
            @PathVariable UUID id,
            Authentication authentication) {

        VagaExterna vagaExistente = service.buscar(id);
        validateOwnership(vagaExistente, authentication);

        VagaExterna vagaExterna = service.desativar(id);
        return ResponseEntity.ok(VagaExternaResponse.fromDomain(vagaExterna));
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable UUID id,
            Authentication authentication) {

        VagaExterna vagaExistente = service.buscar(id);
        validateOwnership(vagaExistente, authentication);

        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private void validateOwnership(VagaExterna vaga, Authentication authentication) {
        UUID authenticatedUserId = securityHelper.getUserIdFromAuthentication(authentication);
        if (!vaga.getCandidatoUsuarioId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("Você não tem permissão para acessar esta vaga externa");
        }
    }
}
