package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.ExperienciaProfissional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExperienciaProfissionalRepository {
    ExperienciaProfissional save(ExperienciaProfissional exp);

    Optional<ExperienciaProfissional> findById(UUID id);

    List<ExperienciaProfissional> listByUsuario(UUID usuarioId);
}
