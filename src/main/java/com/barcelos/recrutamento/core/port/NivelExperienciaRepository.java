package com.barcelos.recrutamento.core.port;

import com.barcelos.recrutamento.core.model.NivelExperiencia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NivelExperienciaRepository {
    NivelExperiencia save(NivelExperiencia nivel);

    Optional<NivelExperiencia> findById(UUID id);

    List<NivelExperiencia> listByOrganizacao(UUID organizacaoId);

    void deleteById(UUID id);
}
