package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.model.Organizacao;
import com.barcelos.recrutamento.core.model.Vaga;
import com.barcelos.recrutamento.core.model.VagaSalva;
import com.barcelos.recrutamento.core.port.OrganizacaoRepository;
import com.barcelos.recrutamento.core.port.VagaRepository;
import com.barcelos.recrutamento.core.port.VagaSalvaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VagaSalvaService {

    private static final int LIMITE_VAGAS_SALVAS = 20;

    private final VagaSalvaRepository vagaSalvaRepository;
    private final VagaRepository vagaRepository;
    private final OrganizacaoRepository organizacaoRepository;

    public VagaSalvaService(VagaSalvaRepository vagaSalvaRepository,
                           VagaRepository vagaRepository,
                           OrganizacaoRepository organizacaoRepository) {
        this.vagaSalvaRepository = vagaSalvaRepository;
        this.vagaRepository = vagaRepository;
        this.organizacaoRepository = organizacaoRepository;
    }

    
    @Transactional
    public VagaSalva salvar(UUID vagaId, UUID usuarioId) {

        vagaRepository.findById(vagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga", vagaId));

        if (vagaSalvaRepository.existsByVagaIdAndUsuarioId(vagaId, usuarioId)) {
            throw new BusinessRuleViolationException("Vaga já está salva");
        }

        long totalSalvas = vagaSalvaRepository.countByUsuarioId(usuarioId);
        if (totalSalvas >= LIMITE_VAGAS_SALVAS) {
            throw new BusinessRuleViolationException(
                String.format("Limite de %d vagas salvas atingido. Remova alguma vaga antes de salvar outra.",
                    LIMITE_VAGAS_SALVAS)
            );
        }

        var vagaSalva = VagaSalva.nova(vagaId, usuarioId);
        return vagaSalvaRepository.save(vagaSalva);
    }

    
    @Transactional
    public void remover(UUID vagaId, UUID usuarioId) {
        var vagaSalva = vagaSalvaRepository.findByVagaIdAndUsuarioId(vagaId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga salva não encontrada"));

        vagaSalvaRepository.delete(vagaSalva);
    }

    
    @Transactional(readOnly = true)
    public List<VagaSalvaDetalhada> listarComDetalhesPorUsuario(UUID usuarioId) {
        List<VagaSalva> vagasSalvas = vagaSalvaRepository.findByUsuarioId(usuarioId);

        return vagasSalvas.stream()
                .map(vs -> {

                    Optional<Vaga> vagaOpt = vagaRepository.findById(vs.getVagaId());
                    if (vagaOpt.isEmpty()) {
                        return null;
                    }

                    Vaga vaga = vagaOpt.get();

                    Optional<Organizacao> orgOpt = organizacaoRepository.findById(vaga.getOrganizacaoId());
                    Organizacao org = orgOpt.orElse(null);

                    return new VagaSalvaDetalhada(vs, vaga, org);
                })
                .filter(vsd -> vsd != null)
                .toList();
    }

    
    public record VagaSalvaDetalhada(
            VagaSalva vagaSalva,
            Vaga vaga,
            Organizacao organizacao
    ) {}

    
    @Transactional(readOnly = true)
    public boolean estaSalva(UUID vagaId, UUID usuarioId) {
        return vagaSalvaRepository.existsByVagaIdAndUsuarioId(vagaId, usuarioId);
    }
}
