package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.*;
import com.barcelos.recrutamento.config.SecurityHelper;
import com.barcelos.recrutamento.core.model.Candidatura;
import com.barcelos.recrutamento.core.port.CandidaturaRepository;
import com.barcelos.recrutamento.core.port.OrganizacaoRepository;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import com.barcelos.recrutamento.core.port.VagaRepository;
import com.barcelos.recrutamento.core.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/candidatos")
public class CandidatoController {
    private final CandidatoService service;
    private final CurriculoService curriculoService;
    private final ConviteProcessoSeletivoService conviteService;
    private final CandidaturaRepository candidaturaRepository;
    private final OrganizacaoRepository organizacaoRepository;
    private final VagaRepository vagaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SecurityHelper securityHelper;

    public CandidatoController(
            CandidatoService service,
            CurriculoService curriculoService,
            ConviteProcessoSeletivoService conviteService,
            CandidaturaRepository candidaturaRepository,
            OrganizacaoRepository organizacaoRepository,
            VagaRepository vagaRepository,
            UsuarioRepository usuarioRepository,
            SecurityHelper securityHelper) {
        this.service = service;
        this.curriculoService = curriculoService;
        this.conviteService = conviteService;
        this.candidaturaRepository = candidaturaRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.vagaRepository = vagaRepository;
        this.usuarioRepository = usuarioRepository;
        this.securityHelper = securityHelper;
    }

    @PostMapping
    public ResponseEntity<CandidatoService.RegistrarResult> registrar(@Valid @RequestBody RegistrarCandidatoRequest request) {
        var result = service.registrar(new CandidatoService.RegistrarCommand(
                request.nome(), request.cpf(), request.email(), request.senha(),
                request.perfilCandidato().dataNascimento(),
                request.perfilCandidato().logradouro(),
                request.perfilCandidato().numero(),
                request.perfilCandidato().complemento(),
                request.perfilCandidato().cep(),
                request.perfilCandidato().cidade(),
                request.perfilCandidato().uf()
        ));
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('CANDIDATO')")
    @PostMapping("/{usuarioId}/historicos")
    public ResponseEntity<CandidatoService.AdicionarHistoricoResult> adicionarHistorico(
            @PathVariable UUID usuarioId,
            @Valid @RequestBody AdicionarHistoricoRequest request,
            Authentication authentication) {

        validateOwnership(usuarioId, authentication);

        var result = service.adicionarHistorico(
                new CandidatoService.AdicionarHistoricoCommand(
                        usuarioId, request.titulo(), request.descricao(), request.instituicao(), request.dataInicio(), request.dataFim()
                )
        );
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('CANDIDATO')")
    @PostMapping("/{usuarioId}/experiencias")
    public ResponseEntity<CandidatoService.AdicionarExperienciaResult> adicionarExperiencia(
            @PathVariable UUID usuarioId,
            @Valid @RequestBody AdicionarExperienciaRequest request,
            Authentication authentication) {

        validateOwnership(usuarioId, authentication);

        var result = service.adicionarExperiencia(
                new CandidatoService.AdicionarExperienciaCommand(
                        usuarioId, request.cargo(), request.empresa(), request.descricao(), request.dataInicio(), request.dataFim()
                )
        );
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('CANDIDATO')")
    @PostMapping("/{usuarioId}/experiencias/{experienciaId}/projetos")
    public ResponseEntity<CandidatoService.AdicionarProjetoExperienciaResult> adicionarProjeto(
            @PathVariable UUID usuarioId,
            @PathVariable UUID experienciaId,
            @Valid @RequestBody AdicionarProjetoExperienciaRequest request,
            Authentication authentication) {

        validateOwnership(usuarioId, authentication);

        var result = service.adicionarProjeto(
                new CandidatoService.AdicionarProjetoExperienciaCommand(
                        experienciaId, request.nome(), request.descricao()
                )
        );
        return ResponseEntity.ok(result);
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @PostMapping("/{usuarioId}/curriculos/gerar-com-ia")
    public ResponseEntity<Map<String, String>> gerarCurriculoComIA(
            @PathVariable UUID usuarioId,
            @Valid @RequestBody GerarCurriculoAIRequest request,
            Authentication authentication) {

        validateOwnership(usuarioId, authentication);

        String curriculo = curriculoService.gerarCurriculoComIA(
                usuarioId,
                request.vagaId(),
                request.modelo(),
                request.observacoes()
        );

        return ResponseEntity.ok(Map.of(
                "curriculo", curriculo,
                "modelo", request.modelo().getNome()
        ));
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @GetMapping("/{usuarioId}/candidaturas")
    public ResponseEntity<List<Candidatura>> listarCandidaturas(
            @PathVariable UUID usuarioId,
            Authentication authentication) {

        validateOwnership(usuarioId, authentication);

        var candidaturas = candidaturaRepository.findByCandidatoUsuarioId(usuarioId);
        return ResponseEntity.ok(candidaturas);
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @GetMapping("/{usuarioId}/convites")
    public ResponseEntity<List<ConviteProcessoResponse>> listarConvites(
            @PathVariable UUID usuarioId,
            Authentication authentication) {

        validateOwnership(usuarioId, authentication);

        var convites = conviteService.listarConvitesPorCandidato(usuarioId);

        var responses = convites.stream()
                .map(c -> {
                    var vaga = vagaRepository.findById(c.getVagaId()).orElse(null);
                    var recrutador = usuarioRepository.findById(c.getRecrutadorUsuarioId()).orElse(null);
                    var organizacao = vaga != null ? organizacaoRepository.findById(vaga.getOrganizacaoId()).orElse(null) : null;

                    return new ConviteProcessoResponse(
                            c.getId(),
                            c.getVagaId(),
                            vaga != null ? vaga.getTitulo() : "Vaga não encontrada",
                            vaga != null ? vaga.getDescricao() : "",
                            organizacao != null ? organizacao.getNome() : "Organização",
                            vaga != null ? vaga.getModalidade().name() : "",
                            vaga != null && vaga.getEndereco() != null ? vaga.getEndereco().cidade() : "",
                            vaga != null && vaga.getEndereco() != null && vaga.getEndereco().uf() != null ? vaga.getEndereco().uf().value() : "",
                            vaga != null ? vaga.getSalario() : null,
                            vaga != null ? vaga.getSalario() : null,
                            recrutador != null ? recrutador.getNome() : "Recrutador",
                            c.getMensagem(),
                            c.getStatus().name(),
                            c.getDataEnvio(),
                            c.getDataExpiracao(),
                            c.getDataResposta()
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @PostMapping("/{usuarioId}/convites/{conviteId}/aceitar")
    public ResponseEntity<ConviteProcessoResponse> aceitarConvite(
            @PathVariable UUID usuarioId,
            @PathVariable UUID conviteId,
            @RequestBody(required = false) com.barcelos.recrutamento.api.dto.AceitarConviteRequest request,
            Authentication authentication) {

        validateOwnership(usuarioId, authentication);

        var convite = request != null && request.conteudoPersonalizado() != null
                ? conviteService.aceitarConvite(conviteId, usuarioId, request.modeloCurriculo(), request.conteudoPersonalizado())
                : conviteService.aceitarConvite(conviteId, usuarioId);

        var vaga = vagaRepository.findById(convite.getVagaId()).orElse(null);
        var recrutador = usuarioRepository.findById(convite.getRecrutadorUsuarioId()).orElse(null);
        var organizacao = vaga != null ? organizacaoRepository.findById(vaga.getOrganizacaoId()).orElse(null) : null;

        return ResponseEntity.ok(new ConviteProcessoResponse(
                convite.getId(),
                convite.getVagaId(),
                vaga != null ? vaga.getTitulo() : "Vaga não encontrada",
                vaga != null ? vaga.getDescricao() : "",
                organizacao != null ? organizacao.getNome() : "Organização",
                vaga != null ? vaga.getModalidade().name() : "",
                vaga != null && vaga.getEndereco() != null ? vaga.getEndereco().cidade() : "",
                vaga != null && vaga.getEndereco() != null && vaga.getEndereco().uf() != null ? vaga.getEndereco().uf().value() : "",
                vaga != null ? vaga.getSalario() : null,
                vaga != null ? vaga.getSalario() : null,
                recrutador != null ? recrutador.getNome() : "Recrutador",
                convite.getMensagem(),
                convite.getStatus().name(),
                convite.getDataEnvio(),
                convite.getDataExpiracao(),
                convite.getDataResposta()
        ));
    }

    
    @PreAuthorize("hasRole('CANDIDATO')")
    @PostMapping("/{usuarioId}/convites/{conviteId}/recusar")
    public ResponseEntity<ConviteProcessoResponse> recusarConvite(
            @PathVariable UUID usuarioId,
            @PathVariable UUID conviteId,
            Authentication authentication) {

        validateOwnership(usuarioId, authentication);

        var convite = conviteService.recusarConvite(conviteId, usuarioId);

        var vaga = vagaRepository.findById(convite.getVagaId()).orElse(null);
        var recrutador = usuarioRepository.findById(convite.getRecrutadorUsuarioId()).orElse(null);
        var organizacao = vaga != null ? organizacaoRepository.findById(vaga.getOrganizacaoId()).orElse(null) : null;

        return ResponseEntity.ok(new ConviteProcessoResponse(
                convite.getId(),
                convite.getVagaId(),
                vaga != null ? vaga.getTitulo() : "Vaga não encontrada",
                vaga != null ? vaga.getDescricao() : "",
                organizacao != null ? organizacao.getNome() : "Organização",
                vaga != null ? vaga.getModalidade().name() : "",
                vaga != null && vaga.getEndereco() != null ? vaga.getEndereco().cidade() : "",
                vaga != null && vaga.getEndereco() != null && vaga.getEndereco().uf() != null ? vaga.getEndereco().uf().value() : "",
                vaga != null ? vaga.getSalario() : null,
                vaga != null ? vaga.getSalario() : null,
                recrutador != null ? recrutador.getNome() : "Recrutador",
                convite.getMensagem(),
                convite.getStatus().name(),
                convite.getDataEnvio(),
                convite.getDataExpiracao(),
                convite.getDataResposta()
        ));
    }

    @PostMapping("/{usuarioId}/competencias")
    @PreAuthorize("hasRole('CANDIDATO')")
    public ResponseEntity<CompetenciaResponse> adicionarCompetencia(
            @PathVariable UUID usuarioId,
            @Valid @RequestBody AdicionarCompetenciaRequest request,
            Authentication authentication) {
        validateOwnership(usuarioId, authentication);

        var result = service.adicionarCompetencia(new CandidatoService.AdicionarCompetenciaCommand(
                usuarioId,
                request.titulo(),
                request.descricao(),
                request.nivel()
        ));

        return ResponseEntity.ok(new CompetenciaResponse(
                result.id(),
                result.titulo(),
                result.descricao(),
                result.nivel()
        ));
    }

    @GetMapping("/{usuarioId}/competencias")
    @PreAuthorize("hasAnyRole('CANDIDATO', 'RECRUTADOR', 'ADMIN')")
    public ResponseEntity<List<CompetenciaResponse>> listarCompetencias(@PathVariable UUID usuarioId) {
        var competencias = service.listarCompetencias(usuarioId);

        var response = competencias.stream()
                .map(c -> new CompetenciaResponse(
                        c.id(),
                        c.titulo(),
                        c.descricao(),
                        c.nivel()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{usuarioId}/competencias/{competenciaId}")
    @PreAuthorize("hasRole('CANDIDATO')")
    public ResponseEntity<CompetenciaResponse> atualizarCompetencia(
            @PathVariable UUID usuarioId,
            @PathVariable UUID competenciaId,
            @Valid @RequestBody AtualizarCompetenciaRequest request,
            Authentication authentication) {
        validateOwnership(usuarioId, authentication);

        var result = service.atualizarCompetencia(
                competenciaId,
                request.titulo(),
                request.descricao(),
                request.nivel()
        );

        return ResponseEntity.ok(new CompetenciaResponse(
                result.id(),
                result.titulo(),
                result.descricao(),
                result.nivel()
        ));
    }

    @DeleteMapping("/{usuarioId}/competencias/{competenciaId}")
    @PreAuthorize("hasRole('CANDIDATO')")
    public ResponseEntity<Void> removerCompetencia(
            @PathVariable UUID usuarioId,
            @PathVariable UUID competenciaId,
            Authentication authentication) {
        validateOwnership(usuarioId, authentication);
        service.removerCompetencia(competenciaId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{usuarioId}/certificados")
    @PreAuthorize("hasRole('CANDIDATO')")
    public ResponseEntity<CertificadoResponse> adicionarCertificado(
            @PathVariable UUID usuarioId,
            @Valid @RequestBody AdicionarCertificadoRequest request,
            Authentication authentication) {
        validateOwnership(usuarioId, authentication);

        var result = service.adicionarCertificado(new CandidatoService.AdicionarCertificadoCommand(
                usuarioId,
                request.titulo(),
                request.instituicao(),
                request.dataEmissao(),
                request.dataValidade(),
                request.descricao()
        ));

        return ResponseEntity.ok(new CertificadoResponse(
                result.id(),
                result.titulo(),
                result.instituicao(),
                result.dataEmissao(),
                result.dataValidade(),
                result.descricao()
        ));
    }

    @GetMapping("/{usuarioId}/certificados")
    @PreAuthorize("hasAnyRole('CANDIDATO', 'RECRUTADOR', 'ADMIN')")
    public ResponseEntity<List<CertificadoResponse>> listarCertificados(@PathVariable UUID usuarioId) {
        var certificados = service.listarCertificados(usuarioId);

        var response = certificados.stream()
                .map(c -> new CertificadoResponse(
                        c.id(),
                        c.titulo(),
                        c.instituicao(),
                        c.dataEmissao(),
                        c.dataValidade(),
                        c.descricao()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{usuarioId}/certificados/{certificadoId}")
    @PreAuthorize("hasRole('CANDIDATO')")
    public ResponseEntity<CertificadoResponse> atualizarCertificado(
            @PathVariable UUID usuarioId,
            @PathVariable UUID certificadoId,
            @Valid @RequestBody AtualizarCertificadoRequest request,
            Authentication authentication) {
        validateOwnership(usuarioId, authentication);

        var result = service.atualizarCertificado(
                certificadoId,
                request.titulo(),
                request.instituicao(),
                request.dataEmissao(),
                request.dataValidade(),
                request.descricao()
        );

        return ResponseEntity.ok(new CertificadoResponse(
                result.id(),
                result.titulo(),
                result.instituicao(),
                result.dataEmissao(),
                result.dataValidade(),
                result.descricao()
        ));
    }

    @DeleteMapping("/{usuarioId}/certificados/{certificadoId}")
    @PreAuthorize("hasRole('CANDIDATO')")
    public ResponseEntity<Void> removerCertificado(
            @PathVariable UUID usuarioId,
            @PathVariable UUID certificadoId,
            Authentication authentication) {
        validateOwnership(usuarioId, authentication);
        service.removerCertificado(certificadoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{usuarioId}/portfolios")
    @PreAuthorize("hasRole('CANDIDATO')")
    public ResponseEntity<PortfolioResponse> adicionarPortfolio(
            @PathVariable UUID usuarioId,
            @Valid @RequestBody AdicionarPortfolioRequest request,
            Authentication authentication) {
        validateOwnership(usuarioId, authentication);

        var result = service.adicionarPortfolio(new CandidatoService.AdicionarPortfolioCommand(
                usuarioId,
                request.titulo(),
                request.link()
        ));

        return ResponseEntity.ok(new PortfolioResponse(
                result.id(),
                result.titulo(),
                result.link()
        ));
    }

    @GetMapping("/{usuarioId}/portfolios")
    @PreAuthorize("hasAnyRole('CANDIDATO', 'RECRUTADOR', 'ADMIN')")
    public ResponseEntity<List<PortfolioResponse>> listarPortfolios(@PathVariable UUID usuarioId) {
        var portfolios = service.listarPortfolios(usuarioId);

        var response = portfolios.stream()
                .map(p -> new PortfolioResponse(
                        p.id(),
                        p.titulo(),
                        p.link()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{usuarioId}/portfolios/{portfolioId}")
    @PreAuthorize("hasRole('CANDIDATO')")
    public ResponseEntity<PortfolioResponse> atualizarPortfolio(
            @PathVariable UUID usuarioId,
            @PathVariable UUID portfolioId,
            @Valid @RequestBody AtualizarPortfolioRequest request,
            Authentication authentication) {
        validateOwnership(usuarioId, authentication);

        var result = service.atualizarPortfolio(
                portfolioId,
                request.titulo(),
                request.link()
        );

        return ResponseEntity.ok(new PortfolioResponse(
                result.id(),
                result.titulo(),
                result.link()
        ));
    }

    @DeleteMapping("/{usuarioId}/portfolios/{portfolioId}")
    @PreAuthorize("hasRole('CANDIDATO')")
    public ResponseEntity<Void> removerPortfolio(
            @PathVariable UUID usuarioId,
            @PathVariable UUID portfolioId,
            Authentication authentication) {
        validateOwnership(usuarioId, authentication);
        service.removerPortfolio(portfolioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{usuarioId}/experiencias")
    @PreAuthorize("hasAnyRole('CANDIDATO', 'RECRUTADOR', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> listarExperiencias(@PathVariable UUID usuarioId) {
        var experiencias = service.listarExperiencias(usuarioId);

        var response = experiencias.stream()
                .map(exp -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", exp.id());
                    map.put("cargo", exp.cargo());
                    map.put("empresa", exp.empresa());
                    map.put("descricao", exp.descricao());
                    map.put("dataInicio", exp.dataInicio());
                    map.put("dataFim", exp.dataFim() != null ? exp.dataFim() : "");
                    return map;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{usuarioId}/experiencias/{experienciaId}")
    @PreAuthorize("hasRole('CANDIDATO')")
    public ResponseEntity<Map<String, Object>> atualizarExperiencia(
            @PathVariable UUID usuarioId,
            @PathVariable UUID experienciaId,
            @Valid @RequestBody AtualizarExperienciaRequest request,
            Authentication authentication) {
        validateOwnership(usuarioId, authentication);

        var result = service.atualizarExperiencia(
                experienciaId,
                request.cargo(),
                request.empresa(),
                request.descricao(),
                request.dataInicio(),
                request.dataFim()
        );

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", result.id());
        response.put("cargo", result.cargo());
        response.put("empresa", result.empresa());
        response.put("descricao", result.descricao());
        response.put("dataInicio", result.dataInicio());
        response.put("dataFim", result.dataFim() != null ? result.dataFim() : "");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{usuarioId}/historicos")
    @PreAuthorize("hasAnyRole('CANDIDATO', 'RECRUTADOR', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> listarHistoricos(@PathVariable UUID usuarioId) {
        var historicos = service.listarHistoricos(usuarioId);

        var response = historicos.stream()
                .map(hist -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", hist.id());
                    map.put("titulo", hist.titulo());
                    map.put("descricao", hist.descricao() != null ? hist.descricao() : "");
                    map.put("instituicao", hist.instituicao());
                    map.put("dataInicio", hist.dataInicio());
                    map.put("dataFim", hist.dataFim() != null ? hist.dataFim() : "");
                    return map;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{usuarioId}/historicos/{historicoId}")
    @PreAuthorize("hasRole('CANDIDATO')")
    public ResponseEntity<Map<String, Object>> atualizarHistorico(
            @PathVariable UUID usuarioId,
            @PathVariable UUID historicoId,
            @Valid @RequestBody AtualizarHistoricoRequest request,
            Authentication authentication) {
        validateOwnership(usuarioId, authentication);

        var result = service.atualizarHistorico(
                historicoId,
                request.titulo(),
                request.instituicao(),
                request.descricao(),
                request.dataInicio(),
                request.dataFim()
        );

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", result.id());
        response.put("titulo", result.titulo());
        response.put("descricao", result.descricao() != null ? result.descricao() : "");
        response.put("instituicao", result.instituicao());
        response.put("dataInicio", result.dataInicio());
        response.put("dataFim", result.dataFim() != null ? result.dataFim() : "");

        return ResponseEntity.ok(response);
    }

    private void validateOwnership(UUID usuarioId, Authentication authentication) {
        UUID authenticatedUserId = securityHelper.getUserIdFromAuthentication(authentication);
        if (!authenticatedUserId.equals(usuarioId)) {
            throw new AccessDeniedException("Você só pode modificar seu próprio perfil");
        }
    }

}
