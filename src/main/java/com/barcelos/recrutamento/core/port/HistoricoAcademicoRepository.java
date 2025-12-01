package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.HistoricoAcademico;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HistoricoAcademicoRepository {
    HistoricoAcademico save(HistoricoAcademico historico);
    Optional<HistoricoAcademico> findById(UUID id);
    List<HistoricoAcademico> listByUsuario(UUID usuarioId);
}
