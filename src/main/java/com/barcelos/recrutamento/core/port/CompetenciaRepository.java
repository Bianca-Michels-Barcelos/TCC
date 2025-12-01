package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.Competencia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompetenciaRepository {
    Competencia save(Competencia competencia);
    Optional<Competencia> findById(UUID id);
    List<Competencia> listByPerfilCandidato(UUID perfilCandidatoId);
    void delete(UUID id);
}
