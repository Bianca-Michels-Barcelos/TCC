package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.api.dto.ProcessoSeletivoComCandidato;
import com.barcelos.recrutamento.core.model.ProcessoSeletivo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessoSeletivoRepository {
    ProcessoSeletivo save(ProcessoSeletivo processo);
    Optional<ProcessoSeletivo> findById(UUID id);
    List<ProcessoSeletivo> findByVagaId(UUID vagaId);
    Optional<ProcessoSeletivo> findByCandidaturaId(UUID candidaturaId);
    List<ProcessoSeletivoComCandidato> findProcessosComCandidatosByVagaId(UUID vagaId);
    Optional<ProcessoSeletivoComCandidato> findProcessoComCandidatoById(UUID processoId);
}