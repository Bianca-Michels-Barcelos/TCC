package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.AvancarEtapaRequest;
import com.barcelos.recrutamento.api.dto.FinalizarProcessoRequest;
import com.barcelos.recrutamento.api.dto.HistoricoEtapaProcessoResponse;
import com.barcelos.recrutamento.config.OrganizacaoSecurityService;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.HistoricoEtapaProcesso;
import com.barcelos.recrutamento.core.model.ProcessoSeletivo;
import com.barcelos.recrutamento.core.port.CandidaturaRepository;
import com.barcelos.recrutamento.core.port.EtapaProcessoRepository;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import com.barcelos.recrutamento.core.port.VagaRepository;
import com.barcelos.recrutamento.core.service.ProcessoSeletivoWorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/processos-seletivos")
public class ProcessoSeletivoController {

    private final ProcessoSeletivoWorkflowService workflowService;
    private final CandidaturaRepository candidaturaRepository;
    private final VagaRepository vagaRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrganizacaoSecurityService orgSecurityService;
    private final EtapaProcessoRepository etapaProcessoRepository;

    public ProcessoSeletivoController(ProcessoSeletivoWorkflowService workflowService,
                                     CandidaturaRepository candidaturaRepository,
                                     VagaRepository vagaRepository,
                                     UsuarioRepository usuarioRepository,
                                     OrganizacaoSecurityService orgSecurityService,
                                     EtapaProcessoRepository etapaProcessoRepository) {
        this.workflowService = workflowService;
        this.candidaturaRepository = candidaturaRepository;
        this.vagaRepository = vagaRepository;
        this.usuarioRepository = usuarioRepository;
        this.orgSecurityService = orgSecurityService;
        this.etapaProcessoRepository = etapaProcessoRepository;
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PostMapping("/{processoId}/avancar")
    public ResponseEntity<ProcessoSeletivo> avancarParaProximaEtapa(
            @PathVariable UUID processoId,
            @Valid @RequestBody FinalizarProcessoRequest request,
            Authentication authentication
    ) {
        validateRecruiterOwnsProcesso(processoId, authentication);

        UUID usuarioId = getUsuarioId(authentication);
        var processo = workflowService.avancarParaProximaEtapa(processoId, usuarioId, request.feedback());

        return ResponseEntity.ok(processo);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PostMapping("/{processoId}/avancar-para-etapa")
    public ResponseEntity<ProcessoSeletivo> avancarParaEtapa(
            @PathVariable UUID processoId,
            @Valid @RequestBody AvancarEtapaRequest request,
            Authentication authentication
    ) {
        validateRecruiterOwnsProcesso(processoId, authentication);

        UUID usuarioId = getUsuarioId(authentication);
        var processo = workflowService.avancarParaEtapa(
            processoId,
            request.etapaId(),
            usuarioId,
            request.feedback()
        );

        return ResponseEntity.ok(processo);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PostMapping("/{processoId}/retornar-para-etapa")
    public ResponseEntity<ProcessoSeletivo> retornarParaEtapa(
            @PathVariable UUID processoId,
            @Valid @RequestBody AvancarEtapaRequest request,
            Authentication authentication
    ) {
        validateRecruiterOwnsProcesso(processoId, authentication);

        UUID usuarioId = getUsuarioId(authentication);
        var processo = workflowService.retornarParaEtapa(
            processoId,
            request.etapaId(),
            usuarioId,
            request.feedback()
        );

        return ResponseEntity.ok(processo);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PostMapping("/{processoId}/finalizar")
    public ResponseEntity<ProcessoSeletivo> finalizar(
            @PathVariable UUID processoId,
            @Valid @RequestBody FinalizarProcessoRequest request,
            Authentication authentication
    ) {
        validateRecruiterOwnsProcesso(processoId, authentication);

        UUID usuarioId = getUsuarioId(authentication);
        var processo = workflowService.finalizar(processoId, usuarioId, request.feedback());

        return ResponseEntity.ok(processo);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PostMapping("/{processoId}/reprovar")
    public ResponseEntity<ProcessoSeletivo> reprovar(
            @PathVariable UUID processoId,
            @Valid @RequestBody FinalizarProcessoRequest request,
            Authentication authentication
    ) {
        validateRecruiterOwnsProcesso(processoId, authentication);

        UUID usuarioId = getUsuarioId(authentication);
        var processo = workflowService.reprovar(processoId, usuarioId, request.feedback());

        return ResponseEntity.ok(processo);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR', 'CANDIDATO')")
    @GetMapping("/{processoId}/historico")
    public ResponseEntity<List<HistoricoEtapaProcessoResponse>> buscarHistorico(
            @PathVariable UUID processoId,
            Authentication authentication
    ) {

        validateUserCanViewProcesso(processoId, authentication);

        var historico = workflowService.buscarHistorico(processoId);
        

        var historicoResponse = historico.stream()
                .map(h -> {

                    String etapaNome;
                    String acao;
                    UUID etapaId;
                    
                    if (h.isPrimeiraMudanca()) {

                        var etapa = etapaProcessoRepository.findById(h.getEtapaNovaId())
                                .orElse(null);
                        etapaNome = etapa != null ? etapa.getNome() : "Etapa inicial";
                        acao = "Processo iniciado";
                        etapaId = h.getEtapaNovaId();
                    } else {

                        var etapaAnterior = etapaProcessoRepository.findById(h.getEtapaAnteriorId())
                                .orElse(null);
                        var etapaNova = etapaProcessoRepository.findById(h.getEtapaNovaId())
                                .orElse(null);
                        
                        etapaNome = etapaAnterior != null ? etapaAnterior.getNome() : "Etapa anterior";
                        String nomeNova = etapaNova != null ? etapaNova.getNome() : "próxima etapa";
                        acao = "Movido para " + nomeNova;
                        etapaId = h.getEtapaAnteriorId();
                    }
                    
                    return new HistoricoEtapaProcessoResponse(
                            h.getId(),
                            h.getProcessoId(),
                            etapaId,
                            etapaNome,
                            h.getUsuarioId(),
                            h.getDataMudanca(),
                            h.getFeedback(),
                            acao
                    );
                })
                .toList();
        
        return ResponseEntity.ok(historicoResponse);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR', 'CANDIDATO')")
    @GetMapping("/{processoId}")
    public ResponseEntity<ProcessoSeletivo> buscarPorId(
            @PathVariable UUID processoId,
            Authentication authentication
    ) {
        validateUserCanViewProcesso(processoId, authentication);
        var processo = workflowService.buscarPorId(processoId);
        return ResponseEntity.ok(processo);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/{processoId}/com-candidato")
    public ResponseEntity<com.barcelos.recrutamento.api.dto.ProcessoSeletivoComCandidato> buscarComCandidatoPorId(
            @PathVariable UUID processoId,
            Authentication authentication
    ) {
        validateRecruiterOwnsProcesso(processoId, authentication);
        var processo = workflowService.buscarComCandidatoPorId(processoId);
        return ResponseEntity.ok(processo);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR', 'CANDIDATO')")
    @GetMapping("/candidatura/{candidaturaId}")
    public ResponseEntity<com.barcelos.recrutamento.api.dto.ProcessoSeletivoComVagaId> buscarPorCandidatura(
            @PathVariable UUID candidaturaId,
            Authentication authentication
    ) {
        var candidatura = candidaturaRepository.findById(candidaturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidatura", candidaturaId));

        var usuarioLogado = getUsuarioId(authentication);
        boolean isCandidato = candidatura.getCandidatoUsuarioId().equals(usuarioLogado);
        boolean isRecrutador = isRecrutadorDaOrganizacao(candidatura.getVagaId(), authentication);

        if (!isCandidato && !isRecrutador) {
            throw new org.springframework.security.access.AccessDeniedException(
                "Você não tem permissão para visualizar este processo seletivo"
            );
        }

        var processo = workflowService.buscarPorCandidatura(candidaturaId);

        var response = new com.barcelos.recrutamento.api.dto.ProcessoSeletivoComVagaId(
                processo,
                candidatura.getVagaId()
        );

        return ResponseEntity.ok(response);
    }

    private void validateRecruiterOwnsProcesso(UUID processoId, Authentication authentication) {
        var processo = workflowService.buscarPorId(processoId);
        var candidatura = candidaturaRepository.findById(processo.getCandidaturaId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidatura", processo.getCandidaturaId()));
        var vaga = vagaRepository.findById(candidatura.getVagaId())
                .orElseThrow(() -> new ResourceNotFoundException("Vaga", candidatura.getVagaId()));

        orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);
    }

    private void validateUserCanViewProcesso(UUID processoId, Authentication authentication) {
        var processo = workflowService.buscarPorId(processoId);
        var candidatura = candidaturaRepository.findById(processo.getCandidaturaId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidatura", processo.getCandidaturaId()));

        var usuarioLogado = getUsuarioId(authentication);
        boolean isCandidato = candidatura.getCandidatoUsuarioId().equals(usuarioLogado);

        if (!isCandidato) {

            var vaga = vagaRepository.findById(candidatura.getVagaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vaga", candidatura.getVagaId()));
            orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);
        }
    }

    private boolean isRecrutadorDaOrganizacao(UUID vagaId, Authentication authentication) {
        try {
            var vaga = vagaRepository.findById(vagaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vaga", vagaId));
            orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private UUID getUsuarioId(Authentication authentication) {
        String email = authentication.getName();
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", email));
        return usuario.getId();
    }
}
