package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.CriarCandidaturaRequest;
import com.barcelos.recrutamento.api.dto.MinhasCandidaturaResponse;
import com.barcelos.recrutamento.config.OrganizacaoSecurityService;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.Candidatura;
import com.barcelos.recrutamento.core.port.CandidaturaRepository;
import com.barcelos.recrutamento.core.port.EtapaProcessoRepository;
import com.barcelos.recrutamento.core.port.OrganizacaoRepository;
import com.barcelos.recrutamento.core.port.ProcessoSeletivoRepository;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import com.barcelos.recrutamento.core.port.VagaRepository;
import com.barcelos.recrutamento.core.service.CandidaturaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class CandidaturaController {

    private final CandidaturaService service;
    private final UsuarioRepository usuarioRepository;
    private final VagaRepository vagaRepository;
    private final CandidaturaRepository candidaturaRepository;
    private final OrganizacaoRepository organizacaoRepository;
    private final ProcessoSeletivoRepository processoSeletivoRepository;
    private final EtapaProcessoRepository etapaProcessoRepository;
    private final OrganizacaoSecurityService orgSecurityService;

    public CandidaturaController(CandidaturaService service,
                                UsuarioRepository usuarioRepository,
                                VagaRepository vagaRepository,
                                CandidaturaRepository candidaturaRepository,
                                OrganizacaoRepository organizacaoRepository,
                                ProcessoSeletivoRepository processoSeletivoRepository,
                                EtapaProcessoRepository etapaProcessoRepository,
                                OrganizacaoSecurityService orgSecurityService) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
        this.vagaRepository = vagaRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.processoSeletivoRepository = processoSeletivoRepository;
        this.etapaProcessoRepository = etapaProcessoRepository;
        this.orgSecurityService = orgSecurityService;
    }

    @PreAuthorize("hasRole('CANDIDATO')")
    @PostMapping("/vagas/{vagaId}/candidaturas")
    public ResponseEntity<Candidatura> criar(
            @PathVariable UUID vagaId,
            @Valid @RequestBody CriarCandidaturaRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", email));

        if (!usuario.getId().equals(request.candidatoUsuarioId())) {
            throw new AccessDeniedException("Você só pode criar candidaturas para si mesmo");
        }

        var candidatura = service.candidatar(
                vagaId, 
                request.candidatoUsuarioId(), 
                request.modeloCurriculo(), 
                request.conteudoPersonalizado()
        );
        return ResponseEntity.ok(candidatura);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/vagas/{vagaId}/candidaturas")
    public ResponseEntity<List<Candidatura>> listarPorVaga(
            @PathVariable UUID vagaId,
            Authentication authentication) {

        var vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga", vagaId));

        orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);

        var candidaturas = service.listarPorVaga(vagaId);
        return ResponseEntity.ok(candidaturas);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR', 'CANDIDATO')")
    @GetMapping("/candidaturas/{candidaturaId}")
    public ResponseEntity<Candidatura> buscarPorId(
            @PathVariable UUID candidaturaId,
            Authentication authentication) {
        var candidatura = candidaturaRepository.findById(candidaturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidatura", candidaturaId));

        String email = authentication.getName();
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", email));

        boolean isCandidato = candidatura.getCandidatoUsuarioId().equals(usuario.getId());

        boolean isRecrutador = false;
        try {
            var vaga = vagaRepository.findById(candidatura.getVagaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vaga", candidatura.getVagaId()));
            orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);
            isRecrutador = true;
        } catch (Exception e) {

        }

        if (!isCandidato && !isRecrutador) {
            throw new AccessDeniedException("Você não tem permissão para visualizar esta candidatura");
        }

        return ResponseEntity.ok(candidatura);
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @GetMapping("/candidaturas/minhas")
    public ResponseEntity<List<MinhasCandidaturaResponse>> listarMinhasCandidaturas(Authentication authentication) {
        String email = authentication.getName();
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", email));

        var candidaturas = candidaturaRepository.findByCandidatoUsuarioId(usuario.getId());

        var candidaturasComDetalhes = candidaturas.stream()
                .map(candidatura -> {

                    var vaga = vagaRepository.findById(candidatura.getVagaId())
                            .orElse(null);

                    if (vaga == null) {
                        return null;
                    }

                    var organizacao = organizacaoRepository.findById(vaga.getOrganizacaoId())
                            .orElse(null);

                    String etapaAtual = null;
                    var processoOpt = processoSeletivoRepository.findByCandidaturaId(candidatura.getId());
                    if (processoOpt.isPresent()) {
                        var processo = processoOpt.get();
                        var etapaOpt = etapaProcessoRepository.findById(processo.getEtapaProcessoAtualId());
                        etapaAtual = etapaOpt.map(etapa -> etapa.getNome()).orElse(null);
                    }

                    var vagaResumo = new MinhasCandidaturaResponse.VagaResumo(
                            vaga.getTitulo(),
                            vaga.getDescricao(),
                            vaga.getModalidade().name(),
                            vaga.getSalario(),
                            new MinhasCandidaturaResponse.OrganizacaoResumo(
                                    organizacao != null ? organizacao.getNome() : "Empresa não encontrada"
                            ),
                            vaga.getEndereco() != null
                                    ? new MinhasCandidaturaResponse.EnderecoResumo(
                                            vaga.getEndereco().cidade(),
                                            vaga.getEndereco().uf().toString()
                                    )
                                    : null
                    );

                    return new MinhasCandidaturaResponse(
                            candidatura.getId(),
                            candidatura.getVagaId(),
                            vagaResumo,
                            candidatura.getStatus().name(),
                            candidatura.getDataCandidatura(),
                            etapaAtual,
                            candidatura.getCompatibilidade()
                    );
                })
                .filter(item -> item != null)
                .toList();

        return ResponseEntity.ok(candidaturasComDetalhes);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PatchMapping("/candidaturas/{candidaturaId}/aceitar")
    public ResponseEntity<Candidatura> aceitar(
            @PathVariable UUID candidaturaId,
            Authentication authentication) {
        validateRecruiterOwnsVaga(candidaturaId, authentication);

        var candidatura = service.aceitar(candidaturaId);
        return ResponseEntity.ok(candidatura);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PatchMapping("/candidaturas/{candidaturaId}/rejeitar")
    public ResponseEntity<Candidatura> rejeitar(
            @PathVariable UUID candidaturaId,
            Authentication authentication) {
        validateRecruiterOwnsVaga(candidaturaId, authentication);

        var candidatura = service.rejeitar(candidaturaId);
        return ResponseEntity.ok(candidatura);
    }

    private void validateRecruiterOwnsVaga(UUID candidaturaId, Authentication authentication) {

        var candidatura = candidaturaRepository.findById(candidaturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidatura", candidaturaId));

        var vaga = vagaRepository.findById(candidatura.getVagaId())
                .orElseThrow(() -> new ResourceNotFoundException("Vaga", candidatura.getVagaId()));

        orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);
    }
}
