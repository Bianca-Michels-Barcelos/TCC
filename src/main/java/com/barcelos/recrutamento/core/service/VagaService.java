package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.api.dto.VagaComEstatisticas;
import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.TipoEtapa;
import com.barcelos.recrutamento.core.model.Vaga;
import com.barcelos.recrutamento.core.model.vo.EnderecoSimples;
import com.barcelos.recrutamento.core.model.vo.Sigla;
import com.barcelos.recrutamento.core.port.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class VagaService {

    private final VagaRepository vagaRepository;
    private final OrganizacaoRepository organizacaoRepository;
    private final MembroOrganizacaoRepository membroOrganizacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VagaBeneficioService vagaBeneficioService;
    private final CompatibilidadeCacheService compatibilidadeCacheService;
    private final EtapaProcessoService etapaProcessoService;
    private final CandidaturaRepository candidaturaRepository;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    public VagaService(VagaRepository vagaRepository,
                      OrganizacaoRepository organizacaoRepository,
                      MembroOrganizacaoRepository membroOrganizacaoRepository,
                      UsuarioRepository usuarioRepository,
                      VagaBeneficioService vagaBeneficioService,
                      CompatibilidadeCacheService compatibilidadeCacheService,
                      EtapaProcessoService etapaProcessoService,
                      CandidaturaRepository candidaturaRepository,
                      EmailService emailService,
                      EmailTemplateService emailTemplateService) {
        this.vagaRepository = vagaRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.membroOrganizacaoRepository = membroOrganizacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.vagaBeneficioService = vagaBeneficioService;
        this.compatibilidadeCacheService = compatibilidadeCacheService;
        this.etapaProcessoService = etapaProcessoService;
        this.candidaturaRepository = candidaturaRepository;
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
    }

    @Transactional
    public Vaga criar(CriarVagaCommand cmd) {
        var org = organizacaoRepository.findById(cmd.organizacaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        var recrutador = usuarioRepository.findById(cmd.recrutadorUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário recrutador não encontrado"));

        if (!membroOrganizacaoRepository.exists(org.getId(), recrutador.getId())) {
            throw new BusinessRuleViolationException("Recrutador não é membro da organização");
        }

        var endereco = (cmd.cidade() != null && !cmd.cidade().isBlank() && cmd.uf() != null && !cmd.uf().isBlank())
                ? new EnderecoSimples(cmd.cidade(), new Sigla(cmd.uf()))
                : null;

        var vaga = Vaga.nova(
                org.getId(),
                recrutador.getId(),
                cmd.titulo(),
                cmd.descricao(),
                cmd.requisitos(),
                cmd.salario(),
                cmd.dataPublicacao(),
                cmd.status(),
                cmd.tipoContrato(),
                cmd.modalidade(),
                cmd.horarioTrabalho(),
                cmd.nivelExperienciaId(),
                endereco
        );

        var vagaSalva = vagaRepository.save(vaga);

        if (cmd.beneficioIds() != null && !cmd.beneficioIds().isEmpty()) {
            for (UUID beneficioId : cmd.beneficioIds()) {
                vagaBeneficioService.adicionar(vagaSalva.getId(), beneficioId);
            }
        }

        etapaProcessoService.criar(
                vagaSalva.getId(),
                "Triagem",
                "Etapa inicial de triagem de currículos",
                TipoEtapa.TRIAGEM_CURRICULO,
                1,
                null,
                null
        );

        compatibilidadeCacheService.calcularParaTodosCandidatos(vagaSalva.getId());

        return vagaSalva;
    }

    @Transactional(readOnly = true)
    public Vaga buscar(UUID vagaId) {
        return vagaRepository.findById(vagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada"));
    }

    @Transactional(readOnly = true)
    public List<Vaga> listarPorRecrutador(UUID recrutadorUsuarioId) {
        return vagaRepository.listByRecrutador(recrutadorUsuarioId);
    }

    
    @Transactional(readOnly = true)
    public Page<VagaComEstatisticas> listarComEstatisticasPorRecrutador(
            UUID recrutadorUsuarioId,
            String status,
            String modalidade,
            String search,
            Pageable pageable) {
        return vagaRepository.findWithStatsByRecrutador(
            recrutadorUsuarioId, status, modalidade, search, pageable
        );
    }

    @Transactional
    public Vaga atualizar(UUID vagaId, AtualizarVagaCommand cmd) {
        var vaga = buscar(vagaId);

        var endereco = (cmd.cidade() != null && !cmd.cidade().isBlank() && cmd.uf() != null && !cmd.uf().isBlank())
                ? new EnderecoSimples(cmd.cidade(), new Sigla(cmd.uf()))
                : null;

        var vagaAtualizada = vaga
                .comTitulo(cmd.titulo())
                .comDescricao(cmd.descricao())
                .comRequisitos(cmd.requisitos())
                .comSalario(cmd.salario())
                .comStatus(cmd.status())
                .comTipoContrato(cmd.tipoContrato())
                .comModalidade(cmd.modalidade())
                .comHorarioTrabalho(cmd.horarioTrabalho())
                .comNivelExperienciaId(cmd.nivelExperienciaId())
                .comEndereco(endereco);

        var vagaSalva = vagaRepository.save(vagaAtualizada);

        if (cmd.beneficioIds() != null) {
            List<UUID> beneficiosAtuais = vagaBeneficioService.listarBeneficiosDaVaga(vagaId);

            for (UUID beneficioId : beneficiosAtuais) {
                vagaBeneficioService.remover(vagaId, beneficioId);
            }

            for (UUID beneficioId : cmd.beneficioIds()) {
                vagaBeneficioService.adicionar(vagaSalva.getId(), beneficioId);
            }
        }

        compatibilidadeCacheService.recalcularVaga(vagaId);

        return vagaSalva;
    }

    @Transactional
    public void desativar(UUID vagaId) {
        var vaga = buscar(vagaId);
        var vagaDesativada = vaga.desativar();
        vagaRepository.save(vagaDesativada);
    }

    @Transactional
    public void ativar(UUID vagaId) {
        var vaga = buscar(vagaId);
        var vagaAtivada = vaga.ativar();
        vagaRepository.save(vagaAtivada);
    }

    @Transactional
    public void deletar(UUID vagaId) {

        buscar(vagaId);
        vagaRepository.deleteById(vagaId);
    }

    @Transactional
    public Vaga fechar(UUID vagaId) {
        var vaga = buscar(vagaId);
        var vagaFechada = vaga.fechar();
        return vagaRepository.save(vagaFechada);
    }

    @Transactional
    public Vaga cancelar(UUID vagaId, String motivo) {
        var vaga = buscar(vagaId);
        var vagaCancelada = vaga.cancelar(motivo);
        var saved = vagaRepository.save(vagaCancelada);

        var organizacao = organizacaoRepository.findById(vaga.getOrganizacaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Organização não encontrada"));

        var candidaturas = candidaturaRepository.listByVaga(vagaId);
        
        for (var candidatura : candidaturas) {
            try {

                var candidato = usuarioRepository.findById(candidatura.getCandidatoUsuarioId())
                        .orElse(null);
                
                if (candidato != null) {

                    String htmlContent = emailTemplateService.renderVagaCancelada(
                            candidato.getNome(),
                            vaga.getTitulo(),
                            organizacao.getNome()
                    );
                    
                    emailService.sendHtmlEmailAsync(
                            candidato.getEmail().value(),
                            "Vaga Cancelada - " + vaga.getTitulo(),
                            htmlContent
                    );
                }
            } catch (Exception e) {

                org.slf4j.LoggerFactory.getLogger(VagaService.class)
                        .error("Erro ao enviar email de cancelamento para candidatura {}: {}", 
                               candidatura.getId(), e.getMessage());
            }
        }

        return saved;
    }

    public record CriarVagaCommand(
            UUID organizacaoId,
            UUID recrutadorUsuarioId,
            String titulo,
            String descricao,
            String requisitos,
            BigDecimal salario,
            LocalDate dataPublicacao,
            com.barcelos.recrutamento.core.model.StatusVaga status,
            com.barcelos.recrutamento.core.model.TipoContrato tipoContrato,
            com.barcelos.recrutamento.core.model.ModalidadeTrabalho modalidade,
            String horarioTrabalho,
            UUID nivelExperienciaId,
            String cidade,
            String uf,
            List<UUID> beneficioIds
    ) {}

    public record AtualizarVagaCommand(
            String titulo,
            String descricao,
            String requisitos,
            BigDecimal salario,
            com.barcelos.recrutamento.core.model.StatusVaga status,
            com.barcelos.recrutamento.core.model.TipoContrato tipoContrato,
            com.barcelos.recrutamento.core.model.ModalidadeTrabalho modalidade,
            String horarioTrabalho,
            UUID nivelExperienciaId,
            String cidade,
            String uf,
            List<UUID> beneficioIds
    ) {}
}
