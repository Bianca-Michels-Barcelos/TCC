package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.api.dto.*;
import com.barcelos.recrutamento.config.OrganizacaoSecurityService;
import com.barcelos.recrutamento.config.SecurityHelper;
import com.barcelos.recrutamento.core.model.Usuario;
import com.barcelos.recrutamento.core.port.CompetenciaRepository;
import com.barcelos.recrutamento.core.port.ExperienciaProfissionalRepository;
import com.barcelos.recrutamento.core.port.OrganizacaoRepository;
import com.barcelos.recrutamento.core.port.UsuarioRepository;
import com.barcelos.recrutamento.core.port.VagaRepository;
import com.barcelos.recrutamento.core.service.BuscaCandidatoService;
import com.barcelos.recrutamento.core.service.ConviteProcessoSeletivoService;
import com.barcelos.recrutamento.core.service.RecrutadorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/organizacoes/{organizacaoId}/recrutadores")
public class RecrutadorController {

    private final RecrutadorService recrutadorService;
    private final BuscaCandidatoService buscaCandidatoService;
    private final ConviteProcessoSeletivoService conviteService;
    private final UsuarioRepository usuarioRepository;
    private final OrganizacaoRepository organizacaoRepository;
    private final VagaRepository vagaRepository;
    private final CompetenciaRepository competenciaRepository;
    private final ExperienciaProfissionalRepository experienciaRepository;
    private final OrganizacaoSecurityService orgSecurityService;
    private final SecurityHelper securityHelper;

    public RecrutadorController(
            RecrutadorService recrutadorService,
            BuscaCandidatoService buscaCandidatoService,
            ConviteProcessoSeletivoService conviteService,
            UsuarioRepository usuarioRepository,
            OrganizacaoRepository organizacaoRepository,
            VagaRepository vagaRepository,
            CompetenciaRepository competenciaRepository,
            ExperienciaProfissionalRepository experienciaRepository,
            OrganizacaoSecurityService orgSecurityService,
            SecurityHelper securityHelper) {
        this.recrutadorService = recrutadorService;
        this.buscaCandidatoService = buscaCandidatoService;
        this.conviteService = conviteService;
        this.usuarioRepository = usuarioRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.vagaRepository = vagaRepository;
        this.competenciaRepository = competenciaRepository;
        this.experienciaRepository = experienciaRepository;
        this.orgSecurityService = orgSecurityService;
        this.securityHelper = securityHelper;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @GetMapping
    public ResponseEntity<List<Usuario>> listar(
            @PathVariable UUID organizacaoId,
            Authentication authentication) {
        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);

        var recrutadores = recrutadorService.listarPorOrganizacao(organizacaoId);
        return ResponseEntity.ok(recrutadores);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> remover(
            @PathVariable UUID organizacaoId,
            @PathVariable UUID usuarioId,
            Authentication authentication) {
        orgSecurityService.validateUserIsAdminOfOrganization(organizacaoId, authentication);
        UUID usuarioLogadoId = securityHelper.getUserIdFromAuthentication(authentication);

        recrutadorService.remover(organizacaoId, usuarioId, usuarioLogadoId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{usuarioId}/papel")
    public ResponseEntity<Usuario> alterarPapel(
            @PathVariable UUID organizacaoId,
            @PathVariable UUID usuarioId,
            @Valid @RequestBody AlterarPapelRequest request,
            Authentication authentication) {
        orgSecurityService.validateUserIsAdminOfOrganization(organizacaoId, authentication);
        UUID usuarioLogadoId = securityHelper.getUserIdFromAuthentication(authentication);

        var usuario = recrutadorService.alterarPapel(organizacaoId, usuarioId, request.papel(), usuarioLogadoId);
        return ResponseEntity.ok(usuario);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{usuarioIdOrigem}/transferir-vagas")
    public ResponseEntity<TransferirVagasResponse> transferirVagas(
            @PathVariable UUID organizacaoId,
            @PathVariable UUID usuarioIdOrigem,
            @Valid @RequestBody TransferirVagasRequest request,
            Authentication authentication) {
        orgSecurityService.validateUserIsAdminOfOrganization(organizacaoId, authentication);

        int quantidadeTransferida = recrutadorService.transferirVagas(organizacaoId, usuarioIdOrigem, request.usuarioIdDestino());
        return ResponseEntity.ok(new TransferirVagasResponse(quantidadeTransferida));
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PostMapping("/buscar-candidatos")
    public ResponseEntity<BuscarCandidatoPageResponse> buscarCandidatos(
            @PathVariable UUID organizacaoId,
            @Valid @RequestBody BuscarCandidatoRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);

        var vaga = vagaRepository.findById(request.vagaId())
                .orElseThrow(() -> new IllegalArgumentException("Vaga não encontrada"));
        if (!vaga.getOrganizacaoId().equals(organizacaoId)) {
            throw new IllegalArgumentException("Vaga não pertence à organização");
        }

        var resultado = buscaCandidatoService.buscarComPaginacao(
                request.vagaId(), 
                request.consulta(), 
                page, 
                size
        );

        var responses = resultado.content().stream()
                .map(r -> {
                    var usuario = r.usuario();
                    var perfil = r.perfil();

                    var competencias = competenciaRepository.listByPerfilCandidato(usuario.getId()).stream()
                            .map(c -> c.getTitulo())
                            .collect(Collectors.toList());

                    var experiencias = experienciaRepository.listByUsuario(usuario.getId()).stream()
                            .map(e -> e.getCargo())
                            .collect(Collectors.toList());

                    boolean jaConvidado = conviteService.existeConvitePendente(request.vagaId(), usuario.getId());

                    return new BuscarCandidatoResponse(
                            usuario.getId(),
                            usuario.getNome(),
                            usuario.getEmail().value(),
                            perfil.getEndereco() != null ? perfil.getEndereco().cidade() : null,
                            perfil.getEndereco() != null ? perfil.getEndereco().uf().value() : null,
                            perfil.getDataNascimento(),
                            competencias,
                            experiencias,
                            r.score(),
                            r.resumo(),
                            jaConvidado
                    );
                })
                .collect(Collectors.toList());

        var pageResponse = new BuscarCandidatoPageResponse(
                responses,
                resultado.currentPage(),
                resultado.totalPages(),
                resultado.totalElements(),
                resultado.size()
        );

        return ResponseEntity.ok(pageResponse);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUTADOR')")
    @PostMapping("/convites")
    public ResponseEntity<ConviteProcessoResponse> enviarConvite(
            @PathVariable UUID organizacaoId,
            @Valid @RequestBody EnviarConviteRequest request,
            Authentication authentication) {

        orgSecurityService.validateUserBelongsToOrganization(organizacaoId, authentication);

        var vaga = vagaRepository.findById(request.vagaId())
                .orElseThrow(() -> new RuntimeException("Vaga não encontrada"));

        if (!vaga.getOrganizacaoId().equals(organizacaoId)) {
            throw new RuntimeException("Vaga não pertence a esta organização");
        }

        UUID recrutadorId = securityHelper.getUserIdFromAuthentication(authentication);

        var convite = conviteService.enviarConvite(
                request.vagaId(),
                recrutadorId,
                request.candidatoUsuarioId(),
                request.mensagem()
        );

        var recrutador = usuarioRepository.findById(recrutadorId).orElse(null);
        var organizacao = organizacaoRepository.findById(organizacaoId).orElse(null);

        return ResponseEntity.ok(new ConviteProcessoResponse(
                convite.getId(),
                convite.getVagaId(),
                vaga.getTitulo(),
                vaga.getDescricao(),
                organizacao != null ? organizacao.getNome() : "Organização",
                vaga.getModalidade().name(),
                vaga.getEndereco() != null ? vaga.getEndereco().cidade() : "",
                vaga.getEndereco() != null && vaga.getEndereco().uf() != null ? vaga.getEndereco().uf().value() : "",
                vaga.getSalario(),
                vaga.getSalario(),
                recrutador != null ? recrutador.getNome() : "Recrutador",
                convite.getMensagem(),
                convite.getStatus().name(),
                convite.getDataEnvio(),
                convite.getDataExpiracao(),
                convite.getDataResposta()
        ));
    }
}
