package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.VagaExterna;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VagaExternaRepository {
    VagaExterna save(VagaExterna vagaExterna);

    Optional<VagaExterna> findById(UUID id);

    List<VagaExterna> listByUsuario(UUID usuarioId);

    void delete(VagaExterna vagaExterna);
}
