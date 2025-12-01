package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.Candidatura;
import com.barcelos.recrutamento.core.model.ProcessoSeletivo;
import com.barcelos.recrutamento.core.model.StatusCandidatura;
import com.barcelos.recrutamento.core.port.CandidaturaRepository;
import com.barcelos.recrutamento.core.port.EtapaProcessoRepository;
import com.barcelos.recrutamento.core.port.ProcessoSeletivoRepository;
import com.barcelos.recrutamento.core.port.VagaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CandidaturaService {

    private final VagaRepository vagaRepository;
    private final CandidaturaRepository candidaturaRepository;
    private final ProcessoSeletivoRepository processoSeletivoRepository;
    private final EtapaProcessoRepository etapaProcessoRepository;
    private final CurriculoService curriculoService;
    private final CompatibilidadeCacheService compatibilidadeCacheService;

    public CandidaturaService(VagaRepository vagaRepository,
                              CandidaturaRepository candidaturaRepository,
                              ProcessoSeletivoRepository processoSeletivoRepository,
                              EtapaProcessoRepository etapaProcessoRepository,
                              CurriculoService curriculoService,
                              CompatibilidadeCacheService compatibilidadeCacheService) {
        this.vagaRepository = vagaRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.processoSeletivoRepository = processoSeletivoRepository;
        this.etapaProcessoRepository = etapaProcessoRepository;
        this.curriculoService = curriculoService;
        this.compatibilidadeCacheService = compatibilidadeCacheService;
    }

    
    @Transactional
    public Candidatura candidatar(UUID vagaId, UUID candidatoUsuarioId, String modeloCurriculo, String conteudoPersonalizado) {

        var vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada"));

        if (vaga.getStatus() == null || !"ABERTA".equals(vaga.getStatus().name())) {
            throw new BusinessRuleViolationException("Vaga não está aberta para candidaturas");
        }

        if (candidaturaRepository.existsByVagaAndCandidato(vagaId, candidatoUsuarioId)) {
            throw new BusinessRuleViolationException("Candidatura já existe para este candidato nesta vaga");
        }

        var candidatura = Candidatura.nova(vagaId, candidatoUsuarioId, null);
        

        var compatibilidadeCache = compatibilidadeCacheService.obterOuCalcular(candidatoUsuarioId, vagaId);
        candidatura = candidatura.comCompatibilidade(compatibilidadeCache.getPercentualCompatibilidade());

        var salva = candidaturaRepository.save(candidatura);

        var etapas = etapaProcessoRepository.findByVagaId(vagaId);
        if (etapas.isEmpty()) {
            throw new BusinessRuleViolationException(
                "Vaga não possui etapas configuradas. Adicione ao menos uma etapa antes de aceitar candidaturas."
            );
        }

        etapas.sort((e1, e2) -> Integer.compare(e1.getOrdem(), e2.getOrdem()));
        var primeiraEtapa = etapas.get(0);

        var processo = ProcessoSeletivo.novo(salva.getId(), primeiraEtapa.getId());
        processoSeletivoRepository.save(processo);

        UUID candidaturaId = salva.getId();
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

        return salva;
    }

    
    @Transactional(readOnly = true)
    public List<Candidatura> listarPorVaga(UUID vagaId) {
        return candidaturaRepository.listByVaga(vagaId);
    }

    
    @Transactional
    public Candidatura aceitar(UUID candidaturaId) {
        var candidatura = candidaturaRepository.findById(candidaturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidatura não encontrada"));

        if (candidatura.getStatus() != StatusCandidatura.PENDENTE) {
            throw new BusinessRuleViolationException("Apenas candidaturas pendentes podem ser aceitas");
        }

        var candidaturaAtualizada = candidatura.comStatus(StatusCandidatura.EM_PROCESSO);
        var saved = candidaturaRepository.save(candidaturaAtualizada);

        return saved;
    }

    
    @Transactional
    public Candidatura rejeitar(UUID candidaturaId) {
        var candidatura = candidaturaRepository.findById(candidaturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidatura não encontrada"));

        StatusCandidatura statusAnterior = candidatura.getStatus();
        var rejeitada = candidatura.rejeitar();
        var saved = candidaturaRepository.save(rejeitada);

        return saved;
    }
}
