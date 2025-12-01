package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.PerfilCandidato;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerfilCandidatoRepository {
    PerfilCandidato save(PerfilCandidato perfil);
    Optional<PerfilCandidato> findByUsuarioId(UUID usuarioId);
    List<PerfilCandidato> findAll();
}
