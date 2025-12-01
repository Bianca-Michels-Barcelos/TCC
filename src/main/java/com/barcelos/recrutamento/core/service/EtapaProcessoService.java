package com.barcelos.recrutamento.core.service;

import com.barcelos.recrutamento.core.model.EtapaProcesso;
import com.barcelos.recrutamento.core.model.StatusEtapa;
import com.barcelos.recrutamento.core.model.TipoEtapa;
import com.barcelos.recrutamento.core.port.EtapaProcessoRepository;
import com.barcelos.recrutamento.core.port.VagaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EtapaProcessoService {

    private final EtapaProcessoRepository repository;
    private final VagaRepository vagaRepository;

    public EtapaProcessoService(
            EtapaProcessoRepository repository,
            VagaRepository vagaRepository
    ) {
        this.repository = repository;
        this.vagaRepository = vagaRepository;
    }

    
    @Transactional
    public EtapaProcesso criar(
            UUID vagaId,
            String nome,
            String descricao,
            TipoEtapa tipo,
            Integer ordem,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {

        var vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new RuntimeException("Vaga não encontrada"));

        int ordemFinal = ordem != null ? ordem : calcularProximaOrdem(vagaId);

        var etapa = EtapaProcesso.criar(
                vagaId, nome, descricao, tipo, ordemFinal,
                dataInicio, dataFim
        );

        return repository.save(etapa);
    }

    
    public List<EtapaProcesso> listarPorVaga(UUID vagaId) {
        return repository.findByVagaId(vagaId);
    }

    
    @Transactional
    public void remover(UUID etapaId) {
        if (!repository.findById(etapaId).isPresent()) {
            throw new RuntimeException("Etapa não encontrada");
        }
        repository.deleteById(etapaId);
    }

    
    private int calcularProximaOrdem(UUID vagaId) {
        var etapas = repository.findByVagaId(vagaId);
        return etapas.stream()
                .mapToInt(EtapaProcesso::getOrdem)
                .max()
                .orElse(0) + 1;
    }
}
