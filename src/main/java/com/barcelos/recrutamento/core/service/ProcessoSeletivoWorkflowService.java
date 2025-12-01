package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.EtapaProcesso;
import com.barcelos.recrutamento.core.model.HistoricoEtapaProcesso;
import com.barcelos.recrutamento.core.model.ProcessoSeletivo;
import com.barcelos.recrutamento.core.model.StatusCandidatura;
import com.barcelos.recrutamento.core.port.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProcessoSeletivoWorkflowService {

    private final ProcessoSeletivoRepository processoRepository;
    private final HistoricoEtapaProcessoRepository historicoRepository;
    private final EtapaProcessoRepository etapaProcessoRepository;
    private final CandidaturaRepository candidaturaRepository;
    private final VagaRepository vagaRepository;
    private final OrganizacaoRepository organizacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    public ProcessoSeletivoWorkflowService(ProcessoSeletivoRepository processoRepository,
                                           HistoricoEtapaProcessoRepository historicoRepository,
                                           EtapaProcessoRepository etapaProcessoRepository,
                                           CandidaturaRepository candidaturaRepository,
                                           VagaRepository vagaRepository,
                                           OrganizacaoRepository organizacaoRepository,
                                           UsuarioRepository usuarioRepository,
                                           EmailService emailService,
                                           EmailTemplateService emailTemplateService) {
        this.processoRepository = processoRepository;
        this.historicoRepository = historicoRepository;
        this.etapaProcessoRepository = etapaProcessoRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.vagaRepository = vagaRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
    }

    
    @Transactional
    public ProcessoSeletivo avancarParaProximaEtapa(UUID processoId, UUID usuarioId, String feedback) {
        var processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo seletivo não encontrado"));

        if (processo.isFinalizado()) {
            throw new BusinessRuleViolationException("Não é possível avançar um processo já finalizado");
        }

        var candidatura = candidaturaRepository.findById(processo.getCandidaturaId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidatura não encontrada"));

        var etapas = etapaProcessoRepository.findByVagaId(candidatura.getVagaId());
        etapas.sort((e1, e2) -> Integer.compare(e1.getOrdem(), e2.getOrdem()));

        int indiceAtual = encontrarIndiceEtapa(etapas, processo.getEtapaProcessoAtualId());

        if (indiceAtual == etapas.size() - 1) {
            throw new BusinessRuleViolationException("Processo já está na última etapa. Use finalizar() para concluir.");
        }

        UUID proximaEtapaProcessoId = etapas.get(indiceAtual + 1).getId();

        return avancarParaEtapa(processoId, proximaEtapaProcessoId, usuarioId, feedback);
    }

    
    @Transactional
    public ProcessoSeletivo avancarParaEtapa(UUID processoId, UUID novaEtapaProcessoId, UUID usuarioId, String feedback) {
        var processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo seletivo não encontrado"));

        if (processo.isFinalizado()) {
            throw new BusinessRuleViolationException("Não é possível avançar um processo já finalizado");
        }

        var candidatura = candidaturaRepository.findById(processo.getCandidaturaId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidatura não encontrada"));

        var etapas = etapaProcessoRepository.findByVagaId(candidatura.getVagaId());
        boolean etapaValida = etapas.stream()
                .anyMatch(ep -> ep.getId().equals(novaEtapaProcessoId));

        if (!etapaValida) {
            throw new BusinessRuleViolationException("Etapa não pertence à vaga desta candidatura");
        }

        if (processo.getEtapaProcessoAtualId().equals(novaEtapaProcessoId)) {
            throw new BusinessRuleViolationException("Processo já está na etapa especificada");
        }

        var historico = HistoricoEtapaProcesso.novo(
                processoId,
                processo.getEtapaProcessoAtualId(),
                novaEtapaProcessoId,
                usuarioId,
                feedback
        );
        historicoRepository.save(historico);

        var processoAtualizado = processo.avancarParaEtapa(novaEtapaProcessoId);
        var saved = processoRepository.save(processoAtualizado);

        if (candidatura.getStatus() == StatusCandidatura.PENDENTE) {
            var candidaturaAtualizada = candidatura.comStatus(StatusCandidatura.EM_PROCESSO);
            candidaturaRepository.save(candidaturaAtualizada);
        }

        if (feedback != null && !feedback.isBlank()) {
            enviarEmailFeedback(candidatura, "AVANCO", feedback);
        }

        return saved;
    }

    
    @Transactional
    public ProcessoSeletivo retornarParaEtapa(UUID processoId, UUID etapaProcessoAnteriorId, UUID usuarioId, String feedback) {
        var processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo seletivo não encontrado"));

        if (processo.isFinalizado()) {
            throw new BusinessRuleViolationException("Não é possível retornar um processo já finalizado");
        }

        var candidatura = candidaturaRepository.findById(processo.getCandidaturaId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidatura não encontrada"));

        var etapas = etapaProcessoRepository.findByVagaId(candidatura.getVagaId());
        boolean etapaValida = etapas.stream()
                .anyMatch(ep -> ep.getId().equals(etapaProcessoAnteriorId));

        if (!etapaValida) {
            throw new BusinessRuleViolationException("Etapa não pertence à vaga desta candidatura");
        }

        if (processo.getEtapaProcessoAtualId().equals(etapaProcessoAnteriorId)) {
            throw new BusinessRuleViolationException("Processo já está na etapa especificada");
        }

        var historico = HistoricoEtapaProcesso.novo(
                processoId,
                processo.getEtapaProcessoAtualId(),
                etapaProcessoAnteriorId,
                usuarioId,
                feedback
        );
        historicoRepository.save(historico);

        var processoAtualizado = processo.comEtapaAtual(etapaProcessoAnteriorId);
        var saved = processoRepository.save(processoAtualizado);

        return saved;
    }

    
    @Transactional
    public ProcessoSeletivo finalizar(UUID processoId, UUID usuarioId, String feedback) {
        var processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo seletivo não encontrado"));

        if (processo.isFinalizado()) {
            throw new BusinessRuleViolationException("Processo já foi finalizado");
        }

        var candidatura = candidaturaRepository.findById(processo.getCandidaturaId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidatura não encontrada"));

        var etapas = etapaProcessoRepository.findByVagaId(candidatura.getVagaId());
        etapas.sort((e1, e2) -> Integer.compare(e1.getOrdem(), e2.getOrdem()));
        
        if (etapas.isEmpty()) {
            throw new BusinessRuleViolationException("Vaga não possui etapas configuradas");
        }
        
        var ultimaEtapa = etapas.get(etapas.size() - 1);
        UUID etapaAnteriorId = processo.getEtapaProcessoAtualId();
        

        if (!etapaAnteriorId.equals(ultimaEtapa.getId())) {
            var historico = HistoricoEtapaProcesso.novo(
                    processoId,
                    etapaAnteriorId,
                    ultimaEtapa.getId(),
                    usuarioId,
                    feedback
            );
            historicoRepository.save(historico);
            

            var processoNaUltimaEtapa = processo.avancarParaEtapa(ultimaEtapa.getId());
            processo = processoRepository.save(processoNaUltimaEtapa);
        } else {

            var historico = HistoricoEtapaProcesso.novo(
                    processoId,
                    etapaAnteriorId,
                    ultimaEtapa.getId(),
                    usuarioId,
                    feedback
            );
            historicoRepository.save(historico);
        }
        

        var ultimaEtapaConcluida = ultimaEtapa.concluir();
        etapaProcessoRepository.save(ultimaEtapaConcluida);

        var processoFinalizado = processo.finalizar();
        var saved = processoRepository.save(processoFinalizado);

        var candidaturaAceita = candidatura.comStatus(StatusCandidatura.ACEITA);
        candidaturaRepository.save(candidaturaAceita);

        enviarEmailFeedback(candidatura, "APROVACAO_FINAL", feedback != null ? feedback : "Parabéns! Você foi selecionado(a) para esta vaga.");

        return saved;
    }

    
    @Transactional
    public ProcessoSeletivo reprovar(UUID processoId, UUID usuarioId, String feedback) {
        var processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo seletivo não encontrado"));

        if (processo.isFinalizado()) {
            throw new BusinessRuleViolationException("Processo já foi finalizado");
        }

        var candidatura = candidaturaRepository.findById(processo.getCandidaturaId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidatura não encontrada"));

        UUID etapaAtualId = processo.getEtapaProcessoAtualId();
        

        var historico = HistoricoEtapaProcesso.novo(
                processoId,
                etapaAtualId,
                etapaAtualId,
                usuarioId,
                feedback
        );
        historicoRepository.save(historico);

        var processoFinalizado = processo.finalizar();
        var saved = processoRepository.save(processoFinalizado);

        var candidaturaRejeitada = candidatura.comStatus(StatusCandidatura.REJEITADA);
        candidaturaRepository.save(candidaturaRejeitada);

        enviarEmailFeedback(candidatura, "REJEICAO", feedback != null ? feedback : "Agradecemos seu interesse. Infelizmente, não poderemos seguir com sua candidatura neste momento.");

        return saved;
    }

    
    private void enviarEmailFeedback(com.barcelos.recrutamento.core.model.Candidatura candidatura, String tipoFeedback, String feedback) {
        try {

            var candidato = usuarioRepository.findById(candidatura.getCandidatoUsuarioId())
                    .orElse(null);
            
            if (candidato == null) {
                return;
            }

            var vaga = vagaRepository.findById(candidatura.getVagaId())
                    .orElse(null);
            
            if (vaga == null) {
                return;
            }

            var organizacao = organizacaoRepository.findById(vaga.getOrganizacaoId())
                    .orElse(null);
            
            if (organizacao == null) {
                return;
            }

            String htmlContent = emailTemplateService.renderFeedbackCandidato(
                    candidato.getNome(),
                    vaga.getTitulo(),
                    organizacao.getNome(),
                    feedback,
                    tipoFeedback
            );

            String subject = switch (tipoFeedback) {
                case "AVANCO" -> "Parabéns! Você avançou no processo seletivo - " + vaga.getTitulo();
                case "APROVACAO_FINAL" -> "🎉 Parabéns! Você foi aprovado(a) - " + vaga.getTitulo();
                case "REJEICAO" -> "Atualização sobre sua candidatura - " + vaga.getTitulo();
                default -> "Atualização sobre sua candidatura - " + vaga.getTitulo();
            };

            emailService.sendHtmlEmailAsync(
                    candidato.getEmail().value(),
                    subject,
                    htmlContent
            );
        } catch (Exception e) {

            org.slf4j.LoggerFactory.getLogger(ProcessoSeletivoWorkflowService.class)
                    .error("Erro ao enviar email de feedback para candidatura {}: {}", 
                           candidatura.getId(), e.getMessage());
        }
    }

    
    @Transactional(readOnly = true)
    public List<HistoricoEtapaProcesso> buscarHistorico(UUID processoId) {
        return historicoRepository.findByProcessoIdOrderByDataMudancaDesc(processoId);
    }

    
    @Transactional(readOnly = true)
    public ProcessoSeletivo buscarPorCandidatura(UUID candidaturaId) {
        return processoRepository.findByCandidaturaId(candidaturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo seletivo não encontrado para esta candidatura"));
    }

    
    @Transactional(readOnly = true)
    public ProcessoSeletivo buscarPorId(UUID processoId) {
        return processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo seletivo não encontrado"));
    }

    
    @Transactional(readOnly = true)
    public List<ProcessoSeletivo> listarPorVaga(UUID vagaId) {
        return processoRepository.findByVagaId(vagaId);
    }

    
    @Transactional(readOnly = true)
    public List<com.barcelos.recrutamento.api.dto.ProcessoSeletivoComCandidato> listarComCandidatosPorVaga(UUID vagaId) {

        return processoRepository.findProcessosComCandidatosByVagaId(vagaId);
    }

    
    @Transactional(readOnly = true)
    public com.barcelos.recrutamento.api.dto.ProcessoSeletivoComCandidato buscarComCandidatoPorId(UUID processoId) {
        return processoRepository.findProcessoComCandidatoById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo seletivo não encontrado"));
    }

    private int encontrarIndiceEtapa(List<EtapaProcesso> etapas, UUID etapaProcessoId) {
        for (int i = 0; i < etapas.size(); i++) {
            if (etapas.get(i).getId().equals(etapaProcessoId)) {
                return i;
            }
        }
        throw new BusinessRuleViolationException("Etapa atual não encontrada nas etapas da vaga");
    }
}
