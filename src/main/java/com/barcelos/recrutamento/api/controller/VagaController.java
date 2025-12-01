package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.*;
import com.barcelos.recrutamento.api.dto.ProcessoSeletivoComCandidato;
import com.barcelos.recrutamento.config.OrganizacaoSecurityService;
import com.barcelos.recrutamento.core.model.ConviteProcessoSeletivo;
import com.barcelos.recrutamento.core.model.EtapaProcesso;
import com.barcelos.recrutamento.core.model.ProcessoSeletivo;
import com.barcelos.recrutamento.core.model.Vaga;
import com.barcelos.recrutamento.core.port.BeneficioOrgRepository;
import com.barcelos.recrutamento.core.port.NivelExperienciaRepository;
import com.barcelos.recrutamento.core.port.OrganizacaoRepository;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import com.barcelos.recrutamento.core.service.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/vagas")
public class VagaController {

    private static final Logger log = LoggerFactory.getLogger(VagaController.class);

    private final VagaService vagaService;
    private final VagaBeneficioService vagaBeneficioService;
    private final EtapaProcessoService etapaProcessoService;
    private final BuscaInteligenteService buscaInteligenteService;
    private final ProcessoSeletivoWorkflowService processoSeletivoService;
    private final ConviteProcessoSeletivoService conviteProcessoService;
    private final OrganizacaoRepository organizacaoRepository;
    private final BeneficioOrgRepository beneficioRepository;
    private final NivelExperienciaRepository nivelExperienciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrganizacaoSecurityService orgSecurityService;
    private final com.barcelos.recrutamento.config.SecurityHelper securityHelper;

    public VagaController(VagaService vagaService,
                         VagaBeneficioService vagaBeneficioService,
                         EtapaProcessoService etapaProcessoService,
                         BuscaInteligenteService buscaInteligenteService,
                         ProcessoSeletivoWorkflowService processoSeletivoService,
                         ConviteProcessoSeletivoService conviteProcessoService,
                         OrganizacaoRepository organizacaoRepository,
                         BeneficioOrgRepository beneficioRepository,
                         NivelExperienciaRepository nivelExperienciaRepository,
                         UsuarioRepository usuarioRepository,
                         OrganizacaoSecurityService orgSecurityService,
                         com.barcelos.recrutamento.config.SecurityHelper securityHelper) {
        this.vagaService = vagaService;
        this.vagaBeneficioService = vagaBeneficioService;
        this.etapaProcessoService = etapaProcessoService;
        this.buscaInteligenteService = buscaInteligenteService;
        this.processoSeletivoService = processoSeletivoService;
        this.conviteProcessoService = conviteProcessoService;
        this.organizacaoRepository = organizacaoRepository;
        this.beneficioRepository = beneficioRepository;
        this.nivelExperienciaRepository = nivelExperienciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.orgSecurityService = orgSecurityService;
        this.securityHelper = securityHelper;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PostMapping
    public ResponseEntity<Vaga> criar(
            @Valid @RequestBody CriarVagaRequest request,
            Authentication authentication) {
        orgSecurityService.validateUserBelongsToOrganization(request.organizacaoId(), authentication);

        var cmd = new VagaService.CriarVagaCommand(
                request.organizacaoId(),
                request.recrutadorUsuarioId(),
                request.titulo(),
                request.descricao(),
                request.requisitos(),
                request.salario(),
                request.dataPublicacao(),
                request.status(),
                request.tipoContrato(),
                request.modalidade(),
                request.horarioTrabalho(),
                request.nivelExperienciaId(),
                request.cidade(),
                request.uf(),
                request.beneficioIds()
        );

        var vaga = vagaService.criar(cmd);
        return ResponseEntity.ok(vaga);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/{vagaId}")
    public ResponseEntity<Vaga> buscar(
            @PathVariable UUID vagaId,
            Authentication authentication) {
        var vaga = vagaService.buscar(vagaId);
        orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);
        return ResponseEntity.ok(vaga);
    }

    
    @GetMapping("/publicas/{vagaId}")
    public ResponseEntity<VagaPublicaResponse> buscarVagaPublica(@PathVariable UUID vagaId) {
        var vaga = vagaService.buscar(vagaId);

        if (vaga.getStatus() != com.barcelos.recrutamento.core.model.StatusVaga.ABERTA) {
            return ResponseEntity.notFound().build();
        }

        var organizacao = organizacaoRepository.findById(vaga.getOrganizacaoId()).orElse(null);
        var organizacaoInfo = organizacao != null
                ? new VagaPublicaResponse.OrganizacaoInfo(organizacao.getId(), organizacao.getNome(), null)
                : null;

        var beneficioIds = vagaBeneficioService.listarBeneficiosDaVaga(vagaId);
        var beneficios = beneficioIds.stream()
                .map(id -> beneficioRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .map(b -> new VagaPublicaResponse.BeneficioInfo(b.getId(), b.getNome(), b.getDescricao()))
                .collect(Collectors.toList());

        VagaPublicaResponse.NivelExperienciaInfo nivelExperienciaInfo = null;
        if (vaga.getNivelExperienciaId() != null) {
            var nivelExp = nivelExperienciaRepository.findById(vaga.getNivelExperienciaId()).orElse(null);
            if (nivelExp != null) {
                nivelExperienciaInfo = new VagaPublicaResponse.NivelExperienciaInfo(
                        nivelExp.getId(),
                        nivelExp.getDescricao()
                );
            }
        }

        var response = new VagaPublicaResponse(
                vaga.getId(),
                vaga.getTitulo(),
                vaga.getDescricao(),
                vaga.getRequisitos(),
                vaga.getSalario(),
                vaga.getModalidade() != null ? vaga.getModalidade().name() : null,
                vaga.getTipoContrato() != null ? vaga.getTipoContrato().name() : null,
                vaga.getHorarioTrabalho(),
                vaga.getStatus().name(),
                vaga.getDataPublicacao(),
                vaga.getEndereco(),
                organizacaoInfo,
                beneficios,
                nivelExperienciaInfo
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/organizacao/{organizacaoId}")
    public ResponseEntity<List<Vaga>> listarPorOrganizacao(
            @PathVariable UUID organizacaoId,
            Authentication authentication) {
        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);

        UUID recrutadorUsuarioId = securityHelper.getUserIdFromAuthentication(authentication);
        var vagas = vagaService.listarPorRecrutador(recrutadorUsuarioId);
        return ResponseEntity.ok(vagas);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/organizacao/{organizacaoId}/with-stats")
    public ResponseEntity<org.springframework.data.domain.Page<VagaComEstatisticas>> listarComEstatisticas(
            @PathVariable UUID organizacaoId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String modalidade,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);

        UUID recrutadorUsuarioId = securityHelper.getUserIdFromAuthentication(authentication);
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var vagas = vagaService.listarComEstatisticasPorRecrutador(recrutadorUsuarioId, status, modalidade, search, pageable);
        return ResponseEntity.ok(vagas);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/organizacao/{organizacaoId}/recentes")
    public ResponseEntity<List<Vaga>> listarVagasRecentes(
            @PathVariable UUID organizacaoId,
            @RequestParam(defaultValue = "10") int limite,
            Authentication authentication) {
        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);

        UUID recrutadorUsuarioId = securityHelper.getUserIdFromAuthentication(authentication);
        var vagas = vagaService.listarPorRecrutador(recrutadorUsuarioId).stream()
                .filter(v -> v.getStatus().name().equals("ABERTA") && v.isAtivo())
                .sorted((v1, v2) -> v2.getDataPublicacao().compareTo(v1.getDataPublicacao()))
                .limit(limite)
                .collect(Collectors.toList());

        return ResponseEntity.ok(vagas);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PutMapping("/{vagaId}")
    public ResponseEntity<Vaga> atualizar(
            @PathVariable UUID vagaId,
            @Valid @RequestBody AtualizarVagaRequest request,
            Authentication authentication) {
        var vaga = vagaService.buscar(vagaId);
        orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);

        var cmd = new VagaService.AtualizarVagaCommand(
                request.titulo(),
                request.descricao(),
                request.requisitos(),
                request.salario(),
                request.status(),
                request.tipoContrato(),
                request.modalidade(),
                request.horarioTrabalho(),
                request.nivelExperienciaId(),
                request.cidade(),
                request.uf(),
                request.beneficioIds()
        );

        var vagaAtualizada = vagaService.atualizar(vagaId, cmd);
        return ResponseEntity.ok(vagaAtualizada);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PatchMapping("/{vagaId}/desativar")
    public ResponseEntity<Void> desativar(
            @PathVariable UUID vagaId,
            Authentication authentication) {
        var vaga = vagaService.buscar(vagaId);
        orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);

        vagaService.desativar(vagaId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PatchMapping("/{vagaId}/ativar")
    public ResponseEntity<Void> ativar(
            @PathVariable UUID vagaId,
            Authentication authentication) {
        var vaga = vagaService.buscar(vagaId);
        orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);

        vagaService.ativar(vagaId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PatchMapping("/{vagaId}/fechar")
    public ResponseEntity<Vaga> fechar(
            @PathVariable UUID vagaId,
            Authentication authentication) {
        var vaga = vagaService.buscar(vagaId);
        orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);

        var vagaFechada = vagaService.fechar(vagaId);
        return ResponseEntity.ok(vagaFechada);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PatchMapping("/{vagaId}/cancelar")
    public ResponseEntity<Vaga> cancelar(
            @PathVariable UUID vagaId,
            @Valid @RequestBody CancelarVagaRequest request,
            Authentication authentication) {
        var vaga = vagaService.buscar(vagaId);
        orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);

        var vagaCancelada = vagaService.cancelar(vagaId, request.motivo());
        return ResponseEntity.ok(vagaCancelada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{vagaId}")
    public ResponseEntity<Void> deletar(
            @PathVariable UUID vagaId,
            Authentication authentication) {
        var vaga = vagaService.buscar(vagaId);
        orgSecurityService.validateUserIsAdminOfOrganization(vaga.getOrganizacaoId(), authentication);

        vagaService.deletar(vagaId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/{vagaId}/beneficios")
    public ResponseEntity<List<UUID>> listarBeneficios(
            @PathVariable UUID vagaId,
            Authentication authentication) {
        var vaga = vagaService.buscar(vagaId);
        orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);

        var beneficios = vagaBeneficioService.listarBeneficiosDaVaga(vagaId);
        return ResponseEntity.ok(beneficios);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PostMapping("/{vagaId}/etapas")
    public ResponseEntity<EtapaProcesso> adicionarEtapa(
            @PathVariable UUID vagaId,
            @Valid @RequestBody AdicionarEtapaVagaRequest request,
            Authentication authentication) {
        var vaga = vagaService.buscar(vagaId);
        orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);

        var etapa = etapaProcessoService.criar(
            vagaId,
            request.nome(),
            request.descricao(),
            request.tipo(),
            request.ordem(),
            request.dataInicio(),
            request.dataFim()
        );
        return ResponseEntity.ok(etapa);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR', 'CANDIDATO')")
    @GetMapping("/{vagaId}/etapas")
    public ResponseEntity<List<EtapaProcesso>> listarEtapas(
            @PathVariable UUID vagaId,
            Authentication authentication) {
        var vaga = vagaService.buscar(vagaId);
        

        boolean isCandidato = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CANDIDATO"));
        
        if (!isCandidato) {
            orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);
        }

        var etapas = etapaProcessoService.listarPorVaga(vagaId);
        return ResponseEntity.ok(etapas);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/{vagaId}/processos-seletivos")
    public ResponseEntity<List<ProcessoSeletivo>> listarProcessosSeletivos(
            @PathVariable UUID vagaId,
            Authentication authentication) {
        var vaga = vagaService.buscar(vagaId);
        orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);

        var processos = processoSeletivoService.listarPorVaga(vagaId);
        return ResponseEntity.ok(processos);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/{vagaId}/processos-seletivos/com-candidatos")
    public ResponseEntity<List<ProcessoSeletivoComCandidato>> listarProcessosComCandidatos(
            @PathVariable UUID vagaId,
            Authentication authentication) {
        var vaga = vagaService.buscar(vagaId);
        orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);

        var processos = processoSeletivoService.listarComCandidatosPorVaga(vagaId);
        return ResponseEntity.ok(processos);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/{vagaId}/candidatos/count")
    public ResponseEntity<Integer> contarCandidatos(
            @PathVariable UUID vagaId,
            Authentication authentication) {
        var vaga = vagaService.buscar(vagaId);
        orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);

        var processos = processoSeletivoService.listarPorVaga(vagaId);
        return ResponseEntity.ok(processos.size());
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping("/{vagaId}/convites")
    public ResponseEntity<List<ConviteProcessoSeletivo>> listarConvites(
            @PathVariable UUID vagaId,
            Authentication authentication) {
        var vaga = vagaService.buscar(vagaId);
        orgSecurityService.validateUserBelongsToOrganization(vaga.getOrganizacaoId(), authentication);

        var convites = conviteProcessoService.listarConvitesPorVaga(vagaId);
        return ResponseEntity.ok(convites);
    }

    
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/busca-inteligente")
    public ResponseEntity<List<BuscaVagaResponse>> buscarInteligente(
            @Valid @RequestBody BuscaVagaRequest request,
            Authentication authentication) {

        UUID usuarioId = null;
        if (authentication != null) {
            String email = authentication.getName();
            var usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario != null) {
                usuarioId = usuario.getId();
                log.debug("Busca inteligente - Usuário autenticado: {} (ID: {})", email, usuarioId);
            } else {
                log.debug("Busca inteligente - Usuário não encontrado para email: {}", email);
            }
        } else {
            log.debug("Busca inteligente - Sem autenticação");
        }

        var resultados = buscaInteligenteService.buscar(request.consulta(), request.limite(), usuarioId);
        log.debug("Busca inteligente - Retornou {} resultados", resultados.size());

        var responses = resultados.stream()
                .map(r -> {
                    var vaga = r.vaga();
                    var organizacao = organizacaoRepository.findById(vaga.getOrganizacaoId()).orElse(null);
                    String nomeOrganizacao = organizacao != null ? organizacao.getNome() : "Não informado";
                    
                    return new BuscaVagaResponse(
                            vaga.getId(),
                            vaga.getTitulo(),
                            vaga.getDescricao(),
                            vaga.getRequisitos(),
                            nomeOrganizacao,
                            vaga.getSalario(),
                            vaga.getModalidade() != null ? vaga.getModalidade().name() : null,
                            vaga.getEndereco() != null ? vaga.getEndereco().cidade() : null,
                            vaga.getEndereco() != null ? vaga.getEndereco().uf().value() : null,
                            r.scoreRelevancia(),
                            r.percentualCompatibilidade(),
                            r.justificativa(),
                            r.usouIA()
                    );
                })
                .filter(response -> {

                    if (request.consulta() != null && !request.consulta().isBlank()) {
                        String termoBusca = request.consulta().toLowerCase();
                        return response.titulo().toLowerCase().contains(termoBusca) ||
                               response.descricao().toLowerCase().contains(termoBusca) ||
                               response.requisitos().toLowerCase().contains(termoBusca) ||
                               response.nomeOrganizacao().toLowerCase().contains(termoBusca);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        if (!responses.isEmpty()) {
            var primeira = responses.get(0);
            log.debug("Primeira vaga - Compatibilidade: {}%, Justificativa: {}, UsouIA: {}", 
                      primeira.percentualCompatibilidade(),
                      (primeira.justificativa() != null ? "SIM" : "NÃO"),
                      primeira.usouIA());
        }

        return ResponseEntity.ok(responses);
    }
}
