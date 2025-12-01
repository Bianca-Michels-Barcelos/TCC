package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.AvaliacaoOrganizacao;
import com.barcelos.recrutamento.core.port.AvaliacaoOrganizacaoRepository;
import com.barcelos.recrutamento.core.port.CandidaturaRepository;
import com.barcelos.recrutamento.core.port.ProcessoSeletivoRepository;
import com.barcelos.recrutamento.core.port.VagaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AvaliacaoOrganizacaoService {

    private final AvaliacaoOrganizacaoRepository avaliacaoRepository;
    private final ProcessoSeletivoRepository processoRepository;
    private final CandidaturaRepository candidaturaRepository;
    private final VagaRepository vagaRepository;

    public AvaliacaoOrganizacaoService(AvaliacaoOrganizacaoRepository avaliacaoRepository,
                                      ProcessoSeletivoRepository processoRepository,
                                      CandidaturaRepository candidaturaRepository,
                                      VagaRepository vagaRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.processoRepository = processoRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.vagaRepository = vagaRepository;
    }

    
    @Transactional
    public AvaliacaoOrganizacao criar(UUID processoId, UUID candidatoUsuarioId, Integer nota, String comentario) {

        var processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo seletivo não encontrado"));

        if (!processo.isFinalizado()) {
            throw new BusinessRuleViolationException(
                "Só é possível avaliar a empresa após a conclusão do processo seletivo"
            );
        }

        var candidatura = candidaturaRepository.findById(processo.getCandidaturaId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidatura não encontrada"));

        if (!candidatura.getCandidatoUsuarioId().equals(candidatoUsuarioId)) {
            throw new BusinessRuleViolationException(
                "Você não tem permissão para avaliar este processo seletivo"
            );
        }

        if (avaliacaoRepository.existsByProcessoId(processoId)) {
            throw new BusinessRuleViolationException(
                "Já existe uma avaliação para este processo seletivo"
            );
        }

        var vaga = vagaRepository.findById(candidatura.getVagaId())
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada"));

        UUID organizacaoId = vaga.getOrganizacaoId();

        var avaliacao = AvaliacaoOrganizacao.nova(processoId, candidatoUsuarioId, organizacaoId, nota, comentario);
        return avaliacaoRepository.save(avaliacao);
    }

    
    @Transactional
    public AvaliacaoOrganizacao atualizar(UUID avaliacaoId, UUID candidatoUsuarioId, Integer nota, String comentario) {
        var avaliacao = avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Avaliação", avaliacaoId));

        if (!avaliacao.getCandidatoUsuarioId().equals(candidatoUsuarioId)) {
            throw new BusinessRuleViolationException(
                "Você não tem permissão para alterar esta avaliação"
            );
        }

        var atualizada = avaliacao.atualizar(nota, comentario);
        return avaliacaoRepository.save(atualizada);
    }

    
    @Transactional(readOnly = true)
    public Optional<AvaliacaoOrganizacao> buscarPorProcesso(UUID processoId) {
        return avaliacaoRepository.findByProcessoId(processoId);
    }

    
    @Transactional(readOnly = true)
    public List<AvaliacaoOrganizacao> listarPorOrganizacao(UUID organizacaoId) {
        return avaliacaoRepository.findByOrganizacaoId(organizacaoId);
    }

    
    @Transactional(readOnly = true)
    public Double calcularNotaMedia(UUID organizacaoId) {
        Double media = avaliacaoRepository.findAverageNotaByOrganizacaoId(organizacaoId);
        return media != null ? media : 0.0;
    }

    
    @Transactional(readOnly = true)
    public long contarAvaliacoes(UUID organizacaoId) {
        return avaliacaoRepository.countByOrganizacaoId(organizacaoId);
    }
}
