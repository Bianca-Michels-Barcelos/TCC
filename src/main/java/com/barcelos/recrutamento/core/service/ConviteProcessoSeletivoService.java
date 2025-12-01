package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.ConviteProcessoSeletivo;
import com.barcelos.recrutamento.core.model.StatusConviteProcesso;
import com.barcelos.recrutamento.core.port.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

@Service
public class ConviteProcessoSeletivoService {

    private static final Logger log = LoggerFactory.getLogger(ConviteProcessoSeletivoService.class);

    private final ConviteProcessoSeletivoRepository conviteRepository;
    private final VagaRepository vagaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CandidaturaRepository candidaturaRepository;
    private final EtapaProcessoRepository etapaProcessoRepository;
    private final ProcessoSeletivoRepository processoSeletivoRepository;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final CompatibilidadeService compatibilidadeService;
    private final CurriculoService curriculoService;

    public ConviteProcessoSeletivoService(
            ConviteProcessoSeletivoRepository conviteRepository,
            VagaRepository vagaRepository,
            UsuarioRepository usuarioRepository,
            CandidaturaRepository candidaturaRepository,
            EtapaProcessoRepository etapaProcessoRepository,
            ProcessoSeletivoRepository processoSeletivoRepository,
            EmailService emailService,
            EmailTemplateService emailTemplateService,
            CompatibilidadeService compatibilidadeService,
            CurriculoService curriculoService
    ) {
        this.conviteRepository = conviteRepository;
        this.vagaRepository = vagaRepository;
        this.usuarioRepository = usuarioRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.etapaProcessoRepository = etapaProcessoRepository;
        this.processoSeletivoRepository = processoSeletivoRepository;
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
        this.compatibilidadeService = compatibilidadeService;
        this.curriculoService = curriculoService;
    }

    
    @Transactional
    public ConviteProcessoSeletivo enviarConvite(
            UUID vagaId,
            UUID recrutadorUsuarioId,
            UUID candidatoUsuarioId,
            String mensagem
    ) {
        log.info("Enviando convite para candidato {} para vaga {}", candidatoUsuarioId, vagaId);

        var vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada"));

        var candidato = usuarioRepository.findById(candidatoUsuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidato não encontrado"));

        var candidaturaExistente = candidaturaRepository.findByVagaIdAndCandidatoUsuarioId(vagaId, candidatoUsuarioId);
        if (candidaturaExistente.isPresent()) {
            throw new BusinessRuleViolationException("Candidato já se candidatou para esta vaga");
        }

        var convitesPendentes = conviteRepository.findByCandidatoUsuarioIdAndStatus(
                candidatoUsuarioId,
                StatusConviteProcesso.PENDENTE
        );

        boolean temConvitePendente = convitesPendentes.stream()
                .anyMatch(c -> c.getVagaId().equals(vagaId));

        if (temConvitePendente) {
            throw new BusinessRuleViolationException("Já existe um convite pendente para este candidato nesta vaga");
        }

        var convite = ConviteProcessoSeletivo.criar(
                vagaId,
                recrutadorUsuarioId,
                candidatoUsuarioId,
                mensagem
        );

        var conviteSalvo = conviteRepository.save(convite);

        try {
            String htmlContent = emailTemplateService.renderConviteProcesso(
                    candidato.getNome(),
                    vaga.getTitulo(),
                    mensagem,
                    conviteSalvo.getId().toString()
            );

            emailService.sendHtmlEmailAsync(
                    candidato.getEmail().value(),
                    "Você foi convidado para um processo seletivo",
                    htmlContent
            );
        } catch (Exception e) {
            log.error("Erro ao enviar email de convite", e);

        }

        log.info("Convite {} enviado com sucesso", conviteSalvo.getId());

        return conviteSalvo;
    }

    
    @Transactional
    public ConviteProcessoSeletivo aceitarConvite(UUID conviteId, UUID candidatoUsuarioId) {
        return aceitarConvite(conviteId, candidatoUsuarioId, null, null);
    }

    
    @Transactional
    public ConviteProcessoSeletivo aceitarConvite(UUID conviteId, UUID candidatoUsuarioId, String modeloCurriculo, String conteudoPersonalizado) {
        log.info("Candidato {} aceitando convite {}", candidatoUsuarioId, conviteId);

        var convite = conviteRepository.findById(conviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado"));

        if (!convite.getCandidatoUsuarioId().equals(candidatoUsuarioId)) {
            throw new BusinessRuleViolationException("Este convite não é para você");
        }

        var candidaturaExistente = candidaturaRepository.findByVagaIdAndCandidatoUsuarioId(
                convite.getVagaId(), 
                candidatoUsuarioId
        );
        
        if (candidaturaExistente.isPresent()) {
            throw new BusinessRuleViolationException("Você já possui uma candidatura para esta vaga");
        }

        var conviteAceito = convite.aceitar();
        var conviteSalvo = conviteRepository.save(conviteAceito);

        try {
            var candidatura = com.barcelos.recrutamento.core.model.Candidatura.nova(
                    convite.getVagaId(),
                    candidatoUsuarioId,
                    null
            );
            

            var compatibilidadeResponse = compatibilidadeService.calcularCompatibilidade(
                    candidatoUsuarioId, 
                    convite.getVagaId()
            );
            candidatura = candidatura.comCompatibilidade(
                    java.math.BigDecimal.valueOf(compatibilidadeResponse.percentualCompatibilidade())
            );
            
            var candidaturaSalva = candidaturaRepository.save(candidatura);
            log.info("Candidatura {} criada automaticamente a partir do convite {} com compatibilidade {}%", 
                    candidaturaSalva.getId(), conviteId, compatibilidadeResponse.percentualCompatibilidade());

            var etapas = etapaProcessoRepository.findByVagaId(convite.getVagaId());
            if (etapas.isEmpty()) {
                throw new BusinessRuleViolationException(
                    "Vaga não possui etapas configuradas. Adicione ao menos uma etapa antes de aceitar candidaturas."
                );
            }

            etapas.sort((e1, e2) -> Integer.compare(e1.getOrdem(), e2.getOrdem()));
            var primeiraEtapa = etapas.get(0);

            var processo = com.barcelos.recrutamento.core.model.ProcessoSeletivo.novo(
                    candidaturaSalva.getId(), 
                    primeiraEtapa.getId()
            );
            processoSeletivoRepository.save(processo);
            log.info("Processo seletivo {} criado para candidatura {}", processo.getId(), candidaturaSalva.getId());

            var candidaturaAtualizada = candidaturaSalva.comStatus(com.barcelos.recrutamento.core.model.StatusCandidatura.EM_PROCESSO);
            candidaturaRepository.save(candidaturaAtualizada);
            log.info("Candidatura {} atualizada para status EM_PROCESSO", candidaturaAtualizada.getId());

            UUID candidaturaId = candidaturaAtualizada.getId();
            if (conteudoPersonalizado != null && !conteudoPersonalizado.isBlank()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        curriculoService.gerarCurriculoPersonalizado(candidaturaId, modeloCurriculo, conteudoPersonalizado);
                    }
                });
            } else {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        curriculoService.gerarEAtualizarCurriculo(candidaturaId);
                    }
                });
            }

        } catch (Exception e) {
            log.error("Erro ao criar candidatura e processo seletivo automaticamente", e);
            throw new BusinessRuleViolationException("Erro ao criar candidatura: " + e.getMessage());
        }

        log.info("Convite {} aceito com sucesso", conviteId);

        return conviteSalvo;
    }

    
    @Transactional
    public ConviteProcessoSeletivo recusarConvite(UUID conviteId, UUID candidatoUsuarioId) {
        log.info("Candidato {} recusando convite {}", candidatoUsuarioId, conviteId);

        var convite = conviteRepository.findById(conviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado"));

        if (!convite.getCandidatoUsuarioId().equals(candidatoUsuarioId)) {
            throw new BusinessRuleViolationException("Este convite não é para você");
        }

        var conviteRecusado = convite.recusar();
        var conviteSalvo = conviteRepository.save(conviteRecusado);

        log.info("Convite {} recusado", conviteId);

        return conviteSalvo;
    }

    
    @Transactional(readOnly = true)
    public List<ConviteProcessoSeletivo> listarConvitesPorCandidato(UUID candidatoUsuarioId) {
        return conviteRepository.findByCandidatoUsuarioId(candidatoUsuarioId);
    }

    
    @Transactional(readOnly = true)
    public List<ConviteProcessoSeletivo> listarConvitesPorVaga(UUID vagaId) {
        return conviteRepository.findByVagaId(vagaId);
    }

    
    @Transactional(readOnly = true)
    public boolean existeConvitePendente(UUID vagaId, UUID candidatoUsuarioId) {
        var convites = conviteRepository.findByVagaIdAndCandidatoUsuarioId(vagaId, candidatoUsuarioId);
        return convites.stream()
                .anyMatch(c -> c.getStatus() == StatusConviteProcesso.PENDENTE);
    }
}
