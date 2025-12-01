package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.Candidatura;
import com.barcelos.recrutamento.core.model.StatusCandidatura;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CandidaturaRepository {
    Candidatura save(Candidatura candidatura);

    boolean existsByVagaAndCandidato(UUID vagaId, UUID candidatoUsuarioId);

    Optional<Candidatura> findById(UUID candidaturaId);

    Optional<Candidatura> findByVagaIdAndCandidatoUsuarioId(UUID vagaId, UUID candidatoUsuarioId);

    List<Candidatura> listByVaga(UUID vagaId);

    List<Candidatura> findByCandidatoUsuarioId(UUID candidatoUsuarioId);
}
