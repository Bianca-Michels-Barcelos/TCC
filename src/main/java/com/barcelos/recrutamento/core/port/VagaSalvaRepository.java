package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.VagaSalva;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VagaSalvaRepository {
    VagaSalva save(VagaSalva vagaSalva);

    Optional<VagaSalva> findByVagaIdAndUsuarioId(UUID vagaId, UUID usuarioId);

    List<VagaSalva> findByUsuarioId(UUID usuarioId);

    long countByUsuarioId(UUID usuarioId);

    void delete(VagaSalva vagaSalva);

    boolean existsByVagaIdAndUsuarioId(UUID vagaId, UUID usuarioId);
}
